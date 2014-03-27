package me.largetalk.http.core;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.*;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AsynQueueServer {
    public static void main(String args[]) throws IOReactorException {
        if (args.length < 1) {
            System.err.println("please specify document root directory");
            System.exit(1);
        }

        File docRoot = new File(args[0]);
        int port = 8080;
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseContent())
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseConnControl()).build();

        UriHttpAsyncRequestHandlerMapper registry = new UriHttpAsyncRequestHandlerMapper();
        registry.register("*", new HttpFileHander(docRoot));
        HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, registry) {
            @Override
            public void connected(final NHttpServerConnection conn) {
                System.out.println(conn + ": connection open");
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
                System.out.println(conn + ": connection closed");
                super.connected(conn);
            }
        };

        NHttpConnectionFactory<DefaultNHttpServerConnection> connFacotry;

        connFacotry = new DefaultNHttpServerConnectionFactory(
                ConnectionConfig.DEFAULT
        );

        IOEventDispatch ioEventDispatch  = new DefaultHttpServerIODispatch(protocolHandler, connFacotry);
        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(1)
                .setSoTimeout(3000)
                .setConnectTimeout(3000)
                .build();
        ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
        try {
            ioReactor.listen(new InetSocketAddress(port));
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            System.err.println("Interrupted");
        } catch (IOException ex) {
            System.err.println("I/O error " + ex.getMessage());
        }
    }

    static class HttpFileHander implements HttpAsyncRequestHandler<HttpRequest> {
        private final File docRoot;

        public HttpFileHander(final File docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public HttpAsyncRequestConsumer<HttpRequest> processRequest(
                final HttpRequest request,
                final HttpContext context
        ) {
            return new BasicAsyncRequestConsumer();
        }

        public void handle(
                final HttpRequest request,
                final HttpAsyncExchange httpexchange,
                final HttpContext context
        ) throws UnsupportedEncodingException, MethodNotSupportedException {
            HttpResponse response = httpexchange.getResponse();
            handleInternal(request, response, context);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
            Timer t = new Timer();
        }

        private void handleInternal(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context
        ) throws MethodNotSupportedException, UnsupportedEncodingException {
            HttpCoreContext coreContext = HttpCoreContext.adapt(context);

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }

            String target = request.getRequestLine().getUri();
            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                NStringEntity entity = new NStringEntity(
                        "<html><body><h1>File " + file.getPath() +
                        " not found</h1></body></html>",
                        ContentType.create("text/html", "UTF-8")
                );
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " not found");
            } else if(!file.canRead() || file.isDirectory()) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                NStringEntity entity = new NStringEntity(
                        "<html><body><h1>File " + file.getPath() +
                                " access denied</h1></body></html>",
                        ContentType.create("text/html", "UTF-8")
                );
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " access denied");
            } else {
                NHttpConnection conn = coreContext.getConnection(NHttpConnection.class);
                response.setStatusCode(HttpStatus.SC_OK);
                NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
                response.setEntity(body);
                System.out.println(conn + ": serving file " + file.getPath());
            }
        }
    }


}
