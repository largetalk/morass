/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.httpserver.sync;

import com.adsame.rtb.lib.configuration.Initializable;
import org.apache.http.protocol.HttpRequestHandler;

public interface SyncRequestHandler extends Initializable, HttpRequestHandler {

    public void dispose();
}
