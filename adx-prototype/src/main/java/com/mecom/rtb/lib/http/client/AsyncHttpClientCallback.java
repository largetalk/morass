/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

public interface AsyncHttpClientCallback extends FutureCallback<HttpResponse> {
}
