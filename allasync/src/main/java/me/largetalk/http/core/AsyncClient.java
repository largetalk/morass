package me.largetalk.http.core;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by largetalk on 2/16/14.
 */
public class AsyncClient {
    public static void main(String args[]) throws InterruptedException, IOException {
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("Test/1.1"))
                .add(new RequestExpectContinue(true)).build();

        HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler,
                ConnectionConfig.DEFAULT);
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        BasicNIOConnPool pool = new BasicNIOConnPool(ioReactor, ConnectionConfig.DEFAULT);
        pool.setDefaultMaxPerRoute(2);
        pool.setMaxTotal(2);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                } catch (IOException ex) {
                    System.err.println("I/O error: " + ex.getMessage());
                }
                System.out.println("shutdown");
            }
        });

        t.start();

        HttpAsyncRequester requester = new HttpAsyncRequester(httpproc);
        HttpHost[] targets = new HttpHost[] {
                new HttpHost("www.baidu.com", 80, "http"),
                new HttpHost("www.verisign.com", 443, "https"),
                new HttpHost("www.google.com", 80, "http")
        };

        final CountDownLatch latch = new CountDownLatch(targets.length);
        for (final HttpHost target: targets) {
            BasicHttpRequest request = new BasicHttpRequest("GET", "/");
            HttpCoreContext coreContext = HttpCoreContext.create();
            requester.execute(
                    new BasicAsyncRequestProducer(target, request),
                    new BasicAsyncResponseConsumer(),
                    pool,
                    coreContext,
                    new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse response) {
                            latch.countDown();
                            System.out.println(target + "->" + response.getStatusLine());
                        }

                        @Override
                        public void failed(Exception e) {
                            latch.countDown();
                            System.out.println(target + "->" + e);
                        }

                        @Override
                        public void cancelled() {
                            latch.countDown();
                            System.out.println(target + " cancelled");

                        }
                    }
            );
        }
        latch.await();
        System.out.println("shutdown i/o reactor");
        ioReactor.shutdown();
        System.out.println("Done");
    }
}
