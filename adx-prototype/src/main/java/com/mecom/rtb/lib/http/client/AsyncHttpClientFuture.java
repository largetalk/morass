/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client;

import java.util.concurrent.Future;
import org.apache.http.HttpResponse;

public interface AsyncHttpClientFuture extends Future<HttpResponse> {
}
