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


import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncHandlerServer {

    public static void main(String args[]) throws IOReactorException {

        int port = 8080;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseContent())
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseConnControl()).build();

        UriHttpAsyncRequestHandlerMapper registry = new UriHttpAsyncRequestHandlerMapper();
        registry.register("*", new HttpFileHander());
        HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, registry) {
            @Override
            public void connected(final NHttpServerConnection conn) {
                //System.out.println(conn + ": connection open");
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
                //System.out.println(conn + ": connection closed");
                super.connected(conn);
            }
        };

        NHttpConnectionFactory<DefaultNHttpServerConnection> connFacotry;

        connFacotry = new DefaultNHttpServerConnectionFactory(
                ConnectionConfig.DEFAULT
        );

        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFacotry);
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
        
        private AtomicInteger num = new AtomicInteger(0);

        public HttpFileHander() {
            super();
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
            Integer cur = num.incrementAndGet();
            System.out.println("begin " + cur + " id: " + Thread.currentThread().getName());
            HttpResponse response = httpexchange.getResponse();
            handleInternal(request, response, context);
            new WorkThread(httpexchange, response, cur).start();
            System.out.println("end " + cur);
            //httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
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

            response.setStatusCode(HttpStatus.SC_OK);
            NStringEntity entity = new NStringEntity(
                    "<html><body><h1>File abc "
                    + " not found</h1></body></html>",
                    ContentType.create("text/html", "UTF-8")
            );
            response.setEntity(entity);
        }
    }
    
    static class WorkThread extends Thread {
        private HttpAsyncExchange httpexchange;
        private HttpResponse response;
        private int cur;
        
        public WorkThread(final HttpAsyncExchange httpexchange, HttpResponse response, int cur) {
            this.httpexchange = httpexchange;
            this.response = response;
            this.cur = cur;
        }
        
        public void run() {
            System.out.println("thread in " + this.cur + " id: " + Thread.currentThread().getName());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AsyncHandlerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
            System.out.println("thread out" + this.cur + " id: " + Thread.currentThread().getName());
        }
    }
}
