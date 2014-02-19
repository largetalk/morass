/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.util;

import com.adsame.rtb.lib.network.client.NettyClient;
import com.adsame.rtb.lib.util.TimedCountDownConditionGenerator;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class AggregationDataReceiver {

    class RequestContext {
        public AggerationHandler callBack;
        public DeSerializer deSerializer;
    }

    private ConcurrentHashMap<String, RequestContext> onGoingRequestMap;
    private ConcurrentHashMap<InetSocketAddress, NettyClient> clients;
    private TimedCountDownConditionGenerator timerPool;

    public AggregationDataReceiver() {
    }

    public void addClient(NettyClient client) {
        InetSocketAddress serverAddress =
                new InetSocketAddress(client.getServerIP(),
                                      client.getServerPort());
        clients.put(serverAddress, client);
    }

    public void removeClient(NettyClient client) {
        InetSocketAddress serverAddress =
                new InetSocketAddress(client.getServerIP(),
                                      client.getServerPort());
        clients.remove(serverAddress, client);
    }

    public void broadcast(Object message,
                          int timeout,
                          Serializer serializer,
                          DeSerializer responseDeSerializer,
                          AggerationHandler callBack) {
        byte data[] = serializer.serialize(message);
    }

    public void onDataReceived(String token, byte data[]) {
        if (!onGoingRequestMap.contains(token)) {
            return;
        }
        RequestContext context = onGoingRequestMap.get(token);
        Object object = context.deSerializer.deSerialize(data);
    }
}
