/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.thrift.server;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.server.TThreadedSelectorServer.Args.AcceptPolicy;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftNonBlockingService implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ThriftNonBlockingService.class);

    private Section section;

    private TProcessor processor;
    private OnStopListener listener;

    private TServer thriftServer;

    public ThriftNonBlockingService(TProcessor processor) {
        this(processor, null);
    }

    public ThriftNonBlockingService(TProcessor processor,
            OnStopListener listener) {
        this.processor = processor;
        this.listener = listener;
    }

    @MetaAnnotation(
            prefix = "server",
            comment = "server configuration")
    public static final class Meta {

        @ShortAnnotation(
                comment = "port of server")
        public static final String PORT = "port";

        @StringAnnotation(
                defaultValue = "selector",
                comment = "server type selector/pool")
        public static final String SERVER_TYPE = "serverType";

        @IntegerAnnotation(
                comment = "number of selector threads")
        public static final String NUM_SELECTOR_THREADS = "numSelectorThreads";

        @IntegerAnnotation(
                comment = "number of worker threads")
        public static final String NUM_WORKER_THREADS = "numWorkerThreads";

        @IntegerAnnotation(
                defaultValue = 4,
                comment = "accept queue length")
        public static final String ACCEPT_QUEUE_LENGTH = "acceptQueueLength";

        @StringAnnotation(
                defaultValue = "fast",
                comment = "accept policy fast or fair")
        public static final String ACCEPT_POLICY = "acceptPolicy";

        //configuration for TThreadPoolServer
        @IntegerAnnotation(
                defaultValue = 5,
                comment = "min number of worker threads")
        public static final String MIN_WORKER_THREADS = "minWorkerThreads";

        @IntegerAnnotation(
                defaultValue = Integer.MAX_VALUE - 1,
                comment = "max number of worker threads")
        public static final String MAX_WORKER_THREADS = "maxWorkerThreads";
    }

    public static interface OnStopListener {

        public void onStop();
    }

    private static enum ServerType {
        SELECTOR,
        POOL
    }

    public static <T> T createHandler(Configuration configuration,
            Class<T> handlerClass) {
        T handler = (T) configuration.createInstance(handlerClass.getName());
        if (handler == null) {
            LOGGER.error("handler create failed");
            throw new IllegalArgumentException("Illegal Configuration");
        }
        return handler;
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        ServerType serverType;
        try {
            serverType = ServerType.valueOf(
                    section.get(Meta.SERVER_TYPE).toString().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("wrong server type ", ex);
        }

        switch (serverType) {
            case SELECTOR:
                thriftServer = initThreadedSelectorServer();
                break;
            case POOL:
                thriftServer = initThreadedPoolServer();
                break;
            default:
                throw new IllegalArgumentException(
                        "unsupport server type" + serverType);
        }
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }

    public void serve() {
        thriftServer.serve();
    }

    public void stop() {
        thriftServer.stop();
        if (listener != null) {
            listener.onStop();
        }
    }

    private TServer initThreadedSelectorServer() {
        short port = (Short) section.get(Meta.PORT);
        int numSelectorThreads = (Integer) section.get(
                Meta.NUM_SELECTOR_THREADS);
        int numWorkerThreads = (Integer) section.get(
                Meta.NUM_WORKER_THREADS);

        int acceptQueueLength = (Integer) section.get(Meta.ACCEPT_QUEUE_LENGTH);

        String acceptPolicy = (String) section.get(Meta.ACCEPT_POLICY);

        TNonblockingServerTransport transport;
        try {
            transport = new TNonblockingServerSocket(port);
        } catch (TTransportException ex) {
            LOGGER.error("transport create failed", ex);
            throw new IllegalArgumentException("transport create failed", ex);
        }

        TThreadedSelectorServer.Args serverArgs
                = new TThreadedSelectorServer.Args(transport);

        serverArgs.processor(processor);

        Factory protocolFactory = new Factory();
        serverArgs.protocolFactory(protocolFactory);

        serverArgs.selectorThreads(numSelectorThreads);

        serverArgs.workerThreads(numWorkerThreads);

        serverArgs.acceptQueueSizePerThread(acceptQueueLength);

        if (acceptPolicy.equalsIgnoreCase("fast")) {
            serverArgs.acceptPolicy(AcceptPolicy.FAST_ACCEPT);
        } else if (acceptPolicy.equalsIgnoreCase("fair")) {
            serverArgs.acceptPolicy(AcceptPolicy.FAIR_ACCEPT);
        } else {
            throw new IllegalArgumentException(
                    "wrong acceptPolicy : " + acceptPolicy);
        }

        // we choose TThreadedSelectorServer
        return new TThreadedSelectorServer(serverArgs);
    }

    private TServer initThreadedPoolServer() {
        short port = (Short) section.get(Meta.PORT);
        int minWorkerThreads = (Integer) section.get(
                Meta.MIN_WORKER_THREADS);
        int maxWorkerThreads = (Integer) section.get(
                Meta.MAX_WORKER_THREADS);

        TServerTransport transport;
        try {
            transport = new TServerSocket(port);
        } catch (TTransportException ex) {
            LOGGER.error("transport create failed", ex);
            throw new IllegalArgumentException("transport create failed", ex);
        }

        TThreadPoolServer.Args serverArgs
                = new TThreadPoolServer.Args(transport);

        serverArgs.processor(processor);

        Factory protocolFactory = new Factory();
        serverArgs.protocolFactory(protocolFactory);

        serverArgs.minWorkerThreads(minWorkerThreads);
        serverArgs.maxWorkerThreads(maxWorkerThreads);

        // we choose TThreadPoolServer
        return new TThreadPoolServer(serverArgs);
    }
}
