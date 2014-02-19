/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.httpserver.async;

import com.adsame.rtb.lib.configuration.Initializable;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;

public interface AsyncRequestHandler extends Initializable,
        HttpAsyncRequestHandler<HttpRequest> {

    public void dispose();
}
