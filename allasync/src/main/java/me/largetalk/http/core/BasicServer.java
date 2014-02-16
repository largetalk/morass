package me.largetalk.http.core;

import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.util.Locale;
import java.security.KeyStore;

public class BasicServer {
    public static void main(String  args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("please specify document root directory");
            System.exit(1);
        }

        String docRoot = args[0];
        int port = 8080;
        if (args.length > 2) {
            port = Integer.parseInt(args[1]);
        }

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("TestServer/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        UriHttpRequestHandlerMapper registry = new UriHttpRequestHandlerMapper();
        registry.register("*", new HttpFileHander(docRoot));

        HttpService httpService = new HttpService(httpproc, registry);

        SSLServerSocketFactory sf = null;
        if (port == 8443) {
            ClassLoader cl = BasicServer.class.getClassLoader();
            URL url = cl.getResource("my.keystore");
            if (url == null) {
                System.out.println("keystore not found");
                System.exit(1);
            }
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(url.openStream(), "secret".toCharArray());
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm()
            );
            kmfactory.init(keyStore, "secret".toCharArray());
            KeyManager keymanagers[] = kmfactory.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keymanagers, null, null);
            sf = sslContext.getServerSocketFactory();
        }

        Thread t = new RequestListenerThread(port, httpService, sf);
        t.setDaemon(false);
        t.start();
    }

    static class HttpFileHander implements HttpRequestHandler {

        private final String docRoot;

        public HttpFileHander(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                System.out.println("Incoming entity content (bytes): " + entityContent.length);
            }

            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>File " + file.getPath() +
                        "not found</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " not found");

            } else if(!file.canRead() || file.isDirectory()) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>access denied</h1></body></html>",
                        ContentType.create("text/html", "UTF-8")
                );
                response.setEntity(entity);
                System.out.println("Cannot read file " + file.getPath());

            } else {
                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
                response.setEntity(body);
                System.out.println("Serving file " + file.getPath());
            }

        }
    }

    static class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serverSocket;
        private final HttpService httpService;

        public RequestListenerThread(final int port, final HttpService httpService,
                                     final SSLServerSocketFactory sf) throws Exception {
            this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serverSocket = sf != null ? sf.createServerSocket(port) : new ServerSocket(port);
            this.httpService = httpService;
        }

        @Override
        public void run() {
            System.out.println("Listening on port " + this.serverSocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    Socket socket = this.serverSocket.accept();
                    System.out.println("incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);

                    Thread t = new WorkThread(this.httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException ex) {
                    System.err.println("I/O error initialising connection thread: " + ex.getMessage());
                    break;
                }
            }
        }

        static class WorkThread extends Thread {
            private final HttpService httpService;
            private final HttpServerConnection conn;

            public WorkThread(final HttpService httpService, final HttpServerConnection conn) {
                super();
                this.httpService = httpService;
                this.conn = conn;
            }

            @Override
            public void run() {
                System.out.println("new connection thread");
                HttpContext context = new BasicHttpContext(null);
                try {
                    while (!Thread.interrupted() && this.conn.isOpen()) {
                        this.httpService.handleRequest(this.conn, context);
                    }
                } catch (ConnectionClosedException ex) {
                    System.err.println("client closed connection");
                } catch (HttpException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        this.conn.shutdown();
                    } catch (IOException ignore) { }
                }

            }
        }
    }
}
