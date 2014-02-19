/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server.httpcomponents;

import com.adsame.rtb.lib.http.server.AsyncHttpServerContext;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;

public class ServerContext implements AsyncHttpServerContext {

    private HttpAsyncExchange httpExchange;

    public ServerContext(HttpAsyncExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    @Override
    public HttpResponse generateResponse() {
        return httpExchange.getResponse();
    }

    @Override
    public void submitResponse(HttpResponse response) {
        httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }
}
