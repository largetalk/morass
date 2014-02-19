/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client.httpcomponents;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.LongAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.http.client.AbstractAsyncHttpClient;
import com.adsame.rtb.lib.http.client.AsyncHttpClientCallback;
import com.adsame.rtb.lib.http.client.AsyncHttpClientFuture;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncHttpClient extends AbstractAsyncHttpClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AsyncHttpClient.class);

    private Section section;

    private int numIOThreads;
    private int maxTotalConnection;
    private int maxConnectionPerHost;

    private long connectionIdleTime;
    private long connectionEvictionInterval;

    private CloseableHttpAsyncClient client;

    private ConnectionEvictor connectionEvictor;

    @MetaAnnotation(
            prefix = "async-http-client",
            comment = "async http client configuration")
    public static final class Meta {

        @IntegerAnnotation(
                defaultValue = 2,
                comment = "number of io threads of async http client")
        public static final String NUM_IO_THREADS = "numIOThreads";

        @LongAnnotation(
                defaultValue = 12 * 60 * 60 * 1000,
                comment = "connection idle time in milli-seconds")
        public static final String CONNECTION_IDLE_TIME =
                "connectionIdleTime";

        @LongAnnotation(
                defaultValue = 1000,
                comment = "connection eviction interval in milli-seconds")
        public static final String CONNECTION_EVICTION_INTERVAL =
                "connectionEvictionInterval";

        @IntegerAnnotation(
                defaultValue = 1024,
                comment = "max total connection")
        public static final String MAX_TOTAL_CONNECTION = "maxTotalConnection";

        @IntegerAnnotation(
                defaultValue = 2,
                comment = "max connection per host")
        public static final String MAX_CONNECTION_PER_HOST =
                "maxConnectionPerHost";

        @StringAnnotation(
                comment = "name of connection reuse strategy")
        public static final String CONNECTION_REUSE_STRATEGY_NAME =
                "connectionReuseStrategyName";
    }

    @Override
    public void initialize(Configuration configuration) {
        super.initialize(configuration);

        section = configuration.getSection(Meta.class);

        numIOThreads = (Integer) section.get(Meta.NUM_IO_THREADS);
        connectionIdleTime = (Long) section.get(Meta.CONNECTION_IDLE_TIME);
        connectionEvictionInterval = (Long) section.get(
                Meta.CONNECTION_EVICTION_INTERVAL);
        maxTotalConnection = (Integer) section.get(Meta.MAX_TOTAL_CONNECTION);
        maxConnectionPerHost = (Integer) section.get(
                Meta.MAX_CONNECTION_PER_HOST);

        IOReactorConfig ioReactorConfiguration = IOReactorConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSoTimeout(socketTimeout)
                .setSoKeepAlive(socketKeepAlive)
                .setSndBufSize(socketBufferSize)
                .setRcvBufSize(socketBufferSize)
                .setTcpNoDelay(tcpNoDelay)
                .setIoThreadCount(numIOThreads)
                .build();
        ConnectingIOReactor ioReactor;
        try {
            ioReactor = new DefaultConnectingIOReactor(
                    ioReactorConfiguration);
        } catch (IOReactorException ex) {
            LOGGER.error("io reactor create failed", ex);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        PoolingNHttpClientConnectionManager connectionManager =
                new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(maxTotalConnection);
        connectionManager.setDefaultMaxPerRoute(maxConnectionPerHost);

        String connectionReuseStrategyName = (String) section.get(
                Meta.CONNECTION_REUSE_STRATEGY_NAME);
        ConnectionReuseStrategy connectionReuseStrategy;
        try {
            Class strategyClass = Class.forName(connectionReuseStrategyName);
            connectionReuseStrategy =
                    (ConnectionReuseStrategy) strategyClass.newInstance();
        } catch (Exception ex) {
            LOGGER.error("connection reuse strategy class {} create failed",
                    connectionReuseStrategyName, ex);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        client = HttpAsyncClients.custom()
                .setConnectionReuseStrategy(connectionReuseStrategy)
                .setConnectionManager(connectionManager)
                .build();

        connectionEvictor = new ConnectionEvictor(connectionManager,
                connectionIdleTime, connectionEvictionInterval);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
        super.saveConfigurationRecursively(outputStream, level + 1);
    }

    @Override
    public Object getOption(String key) {
        if (section.contains(key)) {
            return section.get(key);
        } else {
            return super.getOption(key);
        }
    }

    @Override
    public void setOption(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncHttpClientFuture execute(HttpUriRequest request,
            AsyncHttpClientCallback callback) {
        Future<HttpResponse> future = client.execute(request, callback);
        return new AdaptiveFuture(future);
    }

    @Override
    public void start() {
        client.start();
        connectionEvictor.start();
    }

    @Override
    public void stop() {
        connectionEvictor.close();
        try {
            client.close();
        } catch (IOException ex) {
            LOGGER.error("async http client stop failed", ex);
        }
    }

    @Override
    public boolean isAlive() {
        return client.isRunning();
    }
}
