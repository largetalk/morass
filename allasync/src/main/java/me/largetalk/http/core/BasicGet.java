package me.largetalk.http.core;

import org.apache.http.*;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;

import java.net.Socket;

/**
 * Created by largetalk on 2/13/14.
 */
public class BasicGet {
    public static void main(String[] args) throws Exception {
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("Test/1.1"))
                .add(new RequestExpectContinue(true)).build();

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
        HttpCoreContext coreContext = HttpCoreContext.create();
        HttpHost host = new HttpHost("www.baidu.com", 80);
        coreContext.setTargetHost(host);

        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024);
        ConnectionReuseStrategy connReuseStrategy = DefaultConnectionReuseStrategy.INSTANCE;

        try {
        String[] targets = {
                "/",
                "/plan"
        };
        for (int i = 0; i < targets.length; i++) {
            if (!conn.isOpen()) {
                Socket socket = new Socket(host.getHostName(), host.getPort());
                conn.bind(socket);
            }
            BasicHttpRequest request = new BasicHttpRequest("GET", targets[i]);
            System.out.println(">> Request URI: " + request.getRequestLine().getUri());

            httpexecutor.preProcess(request, httpproc, coreContext);
            HttpResponse response = httpexecutor.execute(request, conn, coreContext);
            httpexecutor.postProcess(response, httpproc, coreContext);

            System.out.println("<< Response: " + response.getStatusLine());
            HeaderIterator iterator = response.headerIterator();
            while (iterator.hasNext()) {
                Header h = (Header) iterator.next();
                System.out.println(h.getName() + " = " + h.getValue());
            }
            System.out.println(EntityUtils.toString(response.getEntity()));
            System.out.println("=======================");
            if (!connReuseStrategy.keepAlive(response, coreContext)) {
                System.out.println("close conn");
                conn.close();
            } else {
                System.out.println("Connection kept alive...");
            }
        }
        } finally {
            conn.close();
        }
    }
}
