/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.server;

import com.adsame.rtb.lib.network.common.EventHandler;
import com.adsame.rtb.lib.network.common.Parameters;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger(NettyServer.class);
    public static final ChannelGroup ALL_CHANNELS =
            new DefaultChannelGroup("Server-Channel-Group");

    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private int port;
    private int workerCount = 1;
    private int mode = Parameters.BLOCKING_HANDLER;
    private EventHandler serverEventHandler = null;
    private AtomicLong messageReceived = new AtomicLong();
    private AtomicLong bytesReceived = new AtomicLong();
    private AtomicLong messageSent = new AtomicLong();
    private AtomicLong bytesSent = new AtomicLong();

    public NettyServer() {
    }

    public NettyServer(int port) {
         this.port = port;
    }

    public NettyServer(int port, EventHandler serverEventHandler) {
        this.port = port;
        this.serverEventHandler = serverEventHandler;
    }

    public void startUp() throws Exception {
        Executor executor = Executors.newCachedThreadPool();
        ServerSocketChannelFactory channelFactory =
                new NioServerSocketChannelFactory(executor,
                                                  executor,
                                                  workerCount);
        serverBootstrap.setFactory(channelFactory);
        /*Other config to be done*/
        Channel channel =
                serverBootstrap.bind(new InetSocketAddress(this.port));
        ALL_CHANNELS.add(channel);
        LOGGER.info("Server is started on port: " + this.port);
        serverBootstrap.setOption(null, mode);
    }

    public void shutDown() throws Exception {
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public EventHandler getHandler() {
        return this.serverEventHandler;
    }

    public void setHandler(EventHandler serverEventHandler) {
        this.serverEventHandler = serverEventHandler;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getWorkerCounter() {
        return workerCount;
    }

    public void setWorkerCounter(int workerCount) {
        this.workerCount = workerCount;
    }

    public Object getOption(String key) {
        return serverBootstrap.getOption(key);
    }

    public void setOption(String key, Object value) {
        serverBootstrap.setOption(key, value);
    }
}
