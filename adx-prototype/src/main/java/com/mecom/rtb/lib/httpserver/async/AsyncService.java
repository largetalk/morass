/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.httpserver.async;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.protocol.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncService extends HttpAsyncService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AsyncService.class);

    private final Boolean logsConnectionInfo;
    private final Map<NHttpServerConnection, Long> startTimeMap;

    public AsyncService(Boolean logsConnectionInfo,
            HttpProcessor httpProcessor,
            ConnectionReuseStrategy connStrategy,
            UriHttpAsyncRequestHandlerMapper registry) {
        super(httpProcessor, connStrategy, null, registry, null);

        this.logsConnectionInfo = logsConnectionInfo;
        this.startTimeMap = Collections.synchronizedMap(
                new HashMap<NHttpServerConnection, Long>());
    }

    @Override
    public void connected(final NHttpServerConnection connection) {
        if (logsConnectionInfo) {
            Long startTime = System.currentTimeMillis();
            startTimeMap.put(connection, startTime);
            long threadID = Thread.currentThread().getId();
            LOGGER.debug("connection {} open in thread {}",
                    connection, threadID);
        }
        super.connected(connection);
    }

    @Override
    public void closed(final NHttpServerConnection connection) {
        if (logsConnectionInfo) {
            Long timeSpan = Long.MAX_VALUE;
            if (startTimeMap.containsKey(connection)) {
                Long endTime = System.currentTimeMillis();
                timeSpan = (endTime - startTimeMap.get(connection)) / 1000;
                startTimeMap.remove(connection);
            }
            long threadID = Thread.currentThread().getId();
            LOGGER.debug("connection {} closed in thread {} with timespan {}s",
                    new Object[]{connection, threadID, timeSpan});
        }
        super.closed(connection);
    }
}
