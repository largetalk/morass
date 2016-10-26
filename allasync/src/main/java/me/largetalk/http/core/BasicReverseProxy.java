package me.largetalk.http.core;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

public class BasicReverseProxy {

    private static final String HTTP_IN_CONN = "http.proxy.in-conn";
    private static final String HTTP_OUT_CONN = "http.proxy.out-conn";
    private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";

    public static void main(String args[]) throws IOException {
        if (args.length < 1) {
            System.err.println("please specified target hostname and port");
            System.exit(1);
        }
        final String hostname = args[0];
        int port = 80;
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        final HttpHost target = new HttpHost(hostname, port);
        final Thread t = new RequestListenerThread(8888, target);
        t.setDaemon(false);
        t.start();
    }

    static class RequestListenerThread extends Thread {
        private final HttpHost target;
        private final ServerSocket serverSocket;
        private final HttpService httpService;

        public RequestListenerThread(final int port, final HttpHost target) throws IOException {
            this.target = target;
            this.serverSocket = new ServerSocket(port);

            final HttpProcessor inhttpproc = new ImmutableHttpProcessor(
                    new HttpRequestInterceptor[] {
                            new RequestContent(),
                            new RequestTargetHost(),
                            new RequestConnControl(),
                            new RequestUserAgent("Test/1.1"),
                            new RequestExpectContinue(true)
                    }
            );

            final HttpProcessor outhttpproc = new ImmutableHttpProcessor(
                    new HttpResponseInterceptor[] {
                            new ResponseContent(),
                            new ResponseServer("Test/1.1"),
                            new ResponseDate(),
                            new ResponseConnControl()
                    }

            );

            final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

            final UriHttpRequestHandlerMapper registry = new UriHttpRequestHandlerMapper();
            registry.register("*", new ProxyHandler(this.target, outhttpproc, httpexecutor));
            this.httpService = new HttpService(inhttpproc, registry);
        }

        @Override
        public void run() {
            System.out.println("Listening on port " + this.serverSocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                final int bufsize = 8 * 1024;
                final Socket insocket = this.serverSocket.accept();
                final DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(bufsize);
                System.out.println("Incoming connection from " + insocket.getInetAddress());
                inconn.bind(insocket);

                final Socket outsocket = new Socket(this.target.getHostName(), this.target.getPort());
                final DefaultBHttpClientConnection outconn = new DefaultBHttpClientConnection(bufsize);
                outconn.bind(outsocket);
                System.out.println("outgoing connection to " + outsocket.getInetAddress());

                final Thread t = new ProxyThread(this.httpService, inconn, outconn);
                t.setDaemon(true);
                t.start();
            } catch (final InterruptedIOException ex) {
                break;
            } catch (final IOException ex) {
                    System.err.println("I/O error initialising connection thread: " + ex.getMessage());
                    break;
                }
            }
        }
    }

    static class ProxyThread extends Thread {
        private final HttpService httpservice;
        private final HttpServerConnection inconn;
        private final HttpClientConnection outconn;

        public ProxyThread(
                final HttpService httpservice,
                final HttpServerConnection inconn,
                final HttpClientConnection outconn
        ) {
            super();
            this.httpservice = httpservice;
            this.inconn = inconn;
            this.outconn = outconn;
        }

        @Override
        public void run() {
            System.out.println("New connection thread");
            final HttpContext context = new BasicHttpContext(null);

            context.setAttribute(HTTP_IN_CONN, this.inconn);
            context.setAttribute(HTTP_OUT_CONN, this.outconn);

            try {
            while (!Thread.interrupted()) {
                if (!this.inconn.isOpen()) {
                    this.outconn.close();
                    break;
                }

                this.httpservice.handleRequest(this.inconn, context);

                final Boolean keepalive = (Boolean) context.getAttribute(HTTP_CONN_KEEPALIVE);
                if (!Boolean.TRUE.equals(keepalive)) {
                    this.outconn.close();
                    this.inconn.close();
                    break;
                }
            }
            } catch (final Exception ex) {
                System.out.println("exception : " + ex.getMessage());
            } finally {
                try {
                    this.inconn.shutdown();
                } catch (final IOException ignore) {}
                try {
                    this.outconn.shutdown();
                } catch (final IOException ignore) {}
            }
        }
    }

    static class ProxyHandler implements HttpRequestHandler {
        private final HttpHost target;
        private final HttpProcessor httpproc;
        private final HttpRequestExecutor httpexecutor;
        private final ConnectionReuseStrategy connStrategy;

        public ProxyHandler(
                final HttpHost target,
                final HttpProcessor httpproc,
                final HttpRequestExecutor httpexecutor
        ) {
            super();
            this.target = target;
            this.httpproc = httpproc;
            this.httpexecutor = httpexecutor;
            this.connStrategy = DefaultConnectionReuseStrategy.INSTANCE;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context
        ) throws IOException, HttpException {
            final HttpClientConnection conn = (HttpClientConnection) context.getAttribute(HTTP_OUT_CONN);
            context.setAttribute(HttpCoreContext.HTTP_CONNECTION, conn);
            context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);

            System.out.println(">> Request URI: " + request.getRequestLine().getUri());
            request.removeHeaders(HTTP.CONTENT_LEN);
            request.removeHeaders(HTTP.TRANSFER_ENCODING);
            request.removeHeaders(HTTP.CONN_DIRECTIVE);
            request.removeHeaders("Keep-Alive");
            request.removeHeaders("Proxy-Authenticate");
            request.removeHeaders("TE");
            request.removeHeaders("Trailers");
            request.removeHeaders("Upgrade");

            this.httpexecutor.preProcess(request, this.httpproc, context);
            final HttpResponse targetResponse = this.httpexecutor.execute(request, conn, context);
            System.out.println("<==> remote: " + targetResponse.getStatusLine());
            this.httpexecutor.postProcess(response, this.httpproc, context);

            targetResponse.removeHeaders(HTTP.CONTENT_LEN);
            targetResponse.removeHeaders(HTTP.TRANSFER_ENCODING);
            targetResponse.removeHeaders(HTTP.CONN_DIRECTIVE);
            targetResponse.removeHeaders("Keep-Alive");
            targetResponse.removeHeaders("TE");
            targetResponse.removeHeaders("Trailers");
            targetResponse.removeHeaders("Upgrade");

            response.setStatusLine(targetResponse.getStatusLine());
            response.setHeaders(targetResponse.getAllHeaders());
            response.setEntity(targetResponse.getEntity());

            System.out.println("<< Response: " + response.getStatusLine());
            final boolean keepalive = this.connStrategy.keepAlive(response, context);
            context.setAttribute(HTTP_CONN_KEEPALIVE, new Boolean(keepalive));
        }
    }
}
