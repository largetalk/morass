/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.common;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EventHandler.class);

    public EventHandler() {
    }

    @Override
    public void messageReceived(ChannelHandlerContext context,
            MessageEvent event) throws Exception {
        Transport transport = (Transport) event.getMessage();
        EventContext eventContext =
                new EventContext(event.getChannel(),
                context, transport.getToken());
        process(eventContext, transport.getData());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context,
            ExceptionEvent exception) throws Exception {
        LOGGER.error(exception.getCause().getMessage(), exception.getCause());
        exception.getChannel().close();
    }

    @Override
    public void channelOpen(ChannelHandlerContext context,
            ChannelStateEvent event) throws Exception {
        LOGGER.info("channel open");
    }

    @Override
    public void channelClosed(ChannelHandlerContext context,
            ChannelStateEvent event) throws Exception {
        LOGGER.info("channel closed");
    }

    @Override
    public void channelConnected(ChannelHandlerContext context,
            ChannelStateEvent event) throws Exception {
        LOGGER.info("channel connected");
        super.channelConnected(context, event);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext context,
            ChannelStateEvent event) throws Exception {
        LOGGER.info("channel disconnectd");
    }

    @Override
    public void childChannelOpen(ChannelHandlerContext context,
            ChildChannelStateEvent event) throws Exception {
        LOGGER.info("child channel open");
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext context,
            ChildChannelStateEvent event) throws Exception {
        LOGGER.info("child channel close");
    }

    public void dispose() {
    }

    protected abstract void process(EventContext context, byte data[]);
}
