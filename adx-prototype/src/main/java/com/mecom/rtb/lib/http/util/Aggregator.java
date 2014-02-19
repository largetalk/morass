/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.util;

import com.adsame.rtb.lib.http.client.AbstractAsyncHttpClient;
import com.adsame.rtb.lib.http.client.AsyncHttpClientCallback;
import com.adsame.rtb.lib.http.client.AsyncHttpClientFuture;
import com.adsame.rtb.lib.util.TimeUtility;
import com.adsame.rtb.lib.util.TimerPool;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class Aggregator {

    private TimerPool timerPool;

    public Aggregator(int threadNums) {
        timerPool = new TimerPool(threadNums);
    }

    public static interface DataHandler {

        public void handle(Data data);
    }

    public static interface LatencyHandler {

        public void handle(HttpUriRequest request, long latency);
    }

    public static class Data {

        private boolean isSealed;

        private int size;
        private HttpResponse responseList[];

        private Data(int size) {
            isSealed = false;

            this.size = size;
            responseList = new HttpResponse[size];
        }

        public int size() {
            return size;
        }

        public HttpResponse get(int index) {
            return responseList[index];
        }

        private synchronized void set(int index, HttpResponse response) {
            if (!isSealed) {
                responseList[index] = response;
            }
        }

        private synchronized void seal() {
            isSealed = true;
        }
    }

    private static class DataDeliveryTask extends TimerTask {

        private Data data;
        private DataHandler dataHandler;

        public DataDeliveryTask(Data data, DataHandler dataHandler) {
            this.data = data;
            this.dataHandler = dataHandler;
        }

        @Override
        public void run() {
            data.seal();
            dataHandler.handle(data);
        }
    }

    private static class RequestCancellationTask extends TimerTask {

        private AsyncHttpClientFuture future;

        public RequestCancellationTask(AsyncHttpClientFuture future) {
            this.future = future;
        }

        @Override
        public void run() {
            future.cancel(true);
        }
    }

    private static class AdaptiveCallback implements AsyncHttpClientCallback {

        private boolean isCalled;

        private Data data;
        private int index;

        private HttpUriRequest request;
        private long startTime;
        private LatencyHandler latencyHandler;

        public AdaptiveCallback(Data data, int index, HttpUriRequest request,
                long startTime, LatencyHandler latencyHandler) {
            isCalled = false;

            this.data = data;
            this.index = index;

            this.request = request;
            this.startTime = startTime;
            this.latencyHandler = latencyHandler;
        }

        @Override
        public synchronized void completed(HttpResponse response) {
            called(response);
        }

        @Override
        public synchronized void failed(Exception ex) {
            called(null);
        }

        @Override
        public synchronized void cancelled() {
            called(null);
        }

        private synchronized void called(HttpResponse response) {
            if (!isCalled) {
                data.set(index, response);
                long endTime = TimeUtility.getTime();
                long latency = endTime - startTime;
                latencyHandler.handle(request, latency);
                isCalled = true;
            }
        }
    }

    public void aggregate(
            ArrayList<AbstractAsyncHttpClient> clientList,
            ArrayList<HttpUriRequest> requestList,
            DataHandler dataHandler, long dataTimeout,
            LatencyHandler latencyHandler, long latencyTimeout) {
        if (clientList == null || requestList == null
                || clientList.size() != requestList.size()
                || dataHandler == null || dataTimeout <= 0
                || latencyHandler == null || latencyTimeout < dataTimeout) {
            throw new IllegalArgumentException("Illegal Argument");
        }

        int size = clientList.size();

        Data data = new Data(size);

        Timer timer = timerPool.getTimer();

        DataDeliveryTask dataDeliveryTask =
                new DataDeliveryTask(data, dataHandler);
        timer.schedule(dataDeliveryTask, dataTimeout);

        long startTime = TimeUtility.getTime();

        for (int i = 0; i < size; i++) {
            AbstractAsyncHttpClient client = clientList.get(i);
            HttpUriRequest request = requestList.get(i);
            AsyncHttpClientCallback callback = new AdaptiveCallback(
                    data, i, request, startTime, latencyHandler);
            AsyncHttpClientFuture future = client.execute(request, callback);

            RequestCancellationTask requestCancellationTask =
                    new RequestCancellationTask(future);
            timer.schedule(requestCancellationTask, latencyTimeout);
        }
    }
}
