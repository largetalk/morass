/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client.httpcomponents;

import com.adsame.rtb.lib.http.client.AsyncHttpClientFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.http.HttpResponse;

public class AdaptiveFuture implements AsyncHttpClientFuture {

    private Future<HttpResponse> future;

    public AdaptiveFuture(Future<HttpResponse> future) {
        this.future = future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public HttpResponse get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public HttpResponse get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }
}
