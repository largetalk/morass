/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server.httpcomponents;

import com.adsame.rtb.lib.http.server.AsyncHttpServerHandler;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

public class AdaptiveHandler
        implements HttpAsyncRequestHandler<HttpRequest> {

    private AsyncHttpServerHandler handler;

    public AdaptiveHandler(AsyncHttpServerHandler handler) {
        this.handler = handler;
    }

    public void dispose() {
        handler.dispose();
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(
            HttpRequest request,
            HttpContext context) throws HttpException, IOException {
        HttpAsyncRequestConsumer consumer = new BasicAsyncRequestConsumer();
        return consumer;
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpExchange,
            HttpContext context) throws HttpException, IOException {
        ServerContext serverContext = new ServerContext(httpExchange);
        handler.handle(request, serverContext);
    }
}
