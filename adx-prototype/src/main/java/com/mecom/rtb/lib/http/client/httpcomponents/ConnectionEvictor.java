/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client.httpcomponents;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;

public class ConnectionEvictor {

    private PoolingNHttpClientConnectionManager connectionManager;
    private long idleTime;
    private long evictionInterval;

    private Lock connectionManagerProtector;
    private ConnectionEvictionTask connectionEvictionTask;
    private Timer timer;

    public ConnectionEvictor(
            PoolingNHttpClientConnectionManager connectionManager,
            long idleTime, long evictionInterval) {
        this.connectionManager = connectionManager;
        this.idleTime = idleTime;
        this.evictionInterval = evictionInterval;

        connectionManagerProtector = new ReentrantLock();
        connectionEvictionTask = new ConnectionEvictionTask();
        timer = new Timer();
    }

    private class ConnectionEvictionTask extends TimerTask {

        @Override
        public void run() {
            connectionManagerProtector.lock();
            try {
                if (connectionManager != null) {
                    connectionManager.closeIdleConnections(idleTime,
                            TimeUnit.MILLISECONDS);
                }
            } finally {
                connectionManagerProtector.unlock();
            }
        }
    }

    public void start() {
        timer.scheduleAtFixedRate(connectionEvictionTask,
                evictionInterval, evictionInterval);
    }

    public void close() {
        connectionManagerProtector.lock();
        try {
            if (connectionManager != null) {
                timer.cancel();
                connectionManager = null;
            }
        } finally {
            connectionManagerProtector.unlock();
        }
    }
}
