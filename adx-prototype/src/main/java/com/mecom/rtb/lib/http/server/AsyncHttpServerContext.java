/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server;

import org.apache.http.HttpResponse;

public interface AsyncHttpServerContext {

    public abstract HttpResponse generateResponse();

    public abstract void submitResponse(HttpResponse response);
}
