/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.client;

import com.adsame.rtb.lib.network.common.EventHandler;
import com.adsame.rtb.lib.network.common.Parameters;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(NettyClient.class);
    public static final ChannelGroup ALL_CHANNELS =
            new DefaultChannelGroup("Server-Channel-Group");

    private ClientBootstrap clientBootstrap = new ClientBootstrap();
    private String serverIP;
    private int serverPort;
    private int connectionPoolSize = 1;
    private int workerCount = 1;
    private int mode = Parameters.BLOCKING_HANDLER;
    private EventHandler clientEventHandler = null;
    private AtomicLong messageReceived = new AtomicLong();
    private AtomicLong bytesReceived = new AtomicLong();
    private AtomicLong messageSent = new AtomicLong();
    private AtomicLong bytesSent = new AtomicLong();

    public NettyClient() {
    }

    public NettyClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public NettyClient(String serverIP, int serverPort,
            EventHandler clientEventHandler) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.clientEventHandler = clientEventHandler;
    }

    public void startUp() throws Exception {
    }

    public void shutDown() throws Exception {
    }

    public void sendNonBlock(byte token[], byte data[]) {
    }

    public String getServerIP() {
        return this.serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public EventHandler getHandler() {
        return this.clientEventHandler;
    }

    public void setHandler(EventHandler clientEventHandler) {
        this.clientEventHandler = clientEventHandler;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getPoolSize() {
        return this.connectionPoolSize;
    }

    public void setPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public int getWorkerCounter() {
        return workerCount;
    }

    public void setWorkerCounter(int workerCount) {
        this.workerCount = workerCount;
    }

    public Object getOption(String key) {
        return clientBootstrap.getOption(key);
    }

    public void setOption(String key, Object value) {
        clientBootstrap.setOption(key, value);
    }
}
