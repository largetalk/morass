/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server;

import com.adsame.rtb.lib.configuration.Initializable;
import org.apache.http.HttpRequest;

public interface AsyncHttpServerHandler extends Initializable {

    public void handle(HttpRequest request, AsyncHttpServerContext context);

    public void dispose();
}
