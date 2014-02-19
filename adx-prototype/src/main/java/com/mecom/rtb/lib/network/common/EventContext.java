/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.common;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class EventContext {

    private Channel channel;
    private ChannelHandlerContext handlerContext;
    private byte token[];

    public EventContext() {
    }

    public EventContext(Channel channel,
                       ChannelHandlerContext handlerContext,
                       byte eventToken[]) {
        this.channel = channel;
        this.handlerContext = handlerContext;
        this.token = eventToken;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public byte[] getToken() {
        return this.token;
    }

    public void setToken(byte token[]) {
        this.token = token;
    }

    public void write(byte data[]) {
        Transport transport = new Transport(this.token, data);
        this.channel.write(transport);
    }
}
