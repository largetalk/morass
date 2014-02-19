/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server.httpcomponents;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.http.server.AbstractAsyncHttpServer;
import com.adsame.rtb.lib.http.server.AsyncHttpServerHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncHttpServer extends AbstractAsyncHttpServer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AsyncHttpServer.class);

    private Section section;

    private int numIOThreads;
    private String handlerPattern;
    private ConnectionReuseStrategy connectionReuseStrategy;
    private AsyncHttpServerHandler requestHandler;
    private AdaptiveHandler adaptiveHandler;

    private IOEventDispatch ioEventDispatch;
    private ListeningIOReactor ioReactor;
    private ServerThread serverThread;

    private Boolean logsConnectionInfo;

    public AsyncHttpServer() {
    }

    @MetaAnnotation(
            prefix = "async-http-server",
            comment = "async http server configuration")
    public static final class Meta {

        @IntegerAnnotation(
                defaultValue = 2,
                comment = "number of io threads of async http server")
        public static final String NUM_IO_THREADS = "numIOThreads";

        @StringAnnotation(
                defaultValue = "*",
                comment = "handler pattern of async http server")
        public static final String HANDLER_PATTERN = "handlerPattern";

        @StringAnnotation(
                comment = "name of connection reuse strategy")
        public static final String CONNECTION_REUSE_STRATEGY_NAME =
                "connectionReuseStrategyName";

        @StringAnnotation(
                comment = "name of request handler")
        public static final String REQUEST_HANDLER_NAME = "requestHandlerName";

        @BooleanAnnotation(
                defaultValue = false,
                comment = "logs connection message or not")
        public static final String LOGS_CONNECTION_INFO = "logsConnectionInfo";
    }

    private class ServerThread extends Thread {

        @Override
        public void run() {
            try {
                // Listen of the given port
                ioReactor.listen(new InetSocketAddress(port));
                // Ready to go!
                ioReactor.execute(ioEventDispatch);
            } catch (InterruptedIOException ex) {
                LOGGER.error("server interrupted", ex);
            } catch (IOException ex) {
                LOGGER.error("I/O error", ex);
            }
        }
    }

    @Override
    public void initialize(Configuration configuration) {
        super.initialize(configuration);

        section = configuration.getSection(Meta.class);

        numIOThreads = (Integer) section.get(Meta.NUM_IO_THREADS);
        handlerPattern = (String) section.get(Meta.HANDLER_PATTERN);

        String connectionReuseStrategyName = (String) section.get(
                Meta.CONNECTION_REUSE_STRATEGY_NAME);
        try {
            Class strategyClass = Class.forName(connectionReuseStrategyName);
            connectionReuseStrategy =
                    (ConnectionReuseStrategy) strategyClass.newInstance();
        } catch (Exception ex) {
            LOGGER.error("connection reuse strategy class {} create failed",
                    connectionReuseStrategyName, ex);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        String requestHandlerName =
                (String) section.get(Meta.REQUEST_HANDLER_NAME);
        requestHandler =
                (AsyncHttpServerHandler) configuration.createInstance(
                    requestHandlerName);
        if (requestHandler == null) {
            LOGGER.error("request handler {} create failed",
                    requestHandlerName);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        logsConnectionInfo = (Boolean) section.get(Meta.LOGS_CONNECTION_INFO);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
        super.saveConfigurationRecursively(outputStream, level + 1);
        requestHandler.saveConfigurationRecursively(outputStream, level + 1);
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
    public void start() throws IOReactorException {
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer())
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();

        // Set up request handlers
        UriHttpAsyncRequestHandlerMapper registry =
                new UriHttpAsyncRequestHandlerMapper();
        adaptiveHandler = new AdaptiveHandler(requestHandler);
        registry.register(handlerPattern, adaptiveHandler);

        // Create server-side HTTP protocol handler
        HttpAsyncService protocolHandler = new AsyncService(logsConnectionInfo,
                httpproc, connectionReuseStrategy, registry);

        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory =
                new DefaultNHttpServerConnectionFactory(
                ConnectionConfig.DEFAULT);

        // Create server-side I/O event dispatch
        ioEventDispatch = new DefaultHttpServerIODispatch(
                protocolHandler, connFactory);

        // Create server-side I/O reactor
        IOReactorConfig ioReactorConfiguration = IOReactorConfig.custom()
                .setSoReuseAddress(true)
                .setSoTimeout(socketTimeout)
                .setSndBufSize(socketBufferSize)
                .setRcvBufSize(socketBufferSize)
                .setTcpNoDelay(tcpNoDelay)
                .setSoKeepAlive(socketKeepAlive)
                .setIoThreadCount(numIOThreads)
                .build();
        ioReactor = new DefaultListeningIOReactor(ioReactorConfiguration);

        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    public void stop() throws IOException {
        ioReactor.shutdown();
        adaptiveHandler.dispose();
    }

    @Override
    public boolean isAlive() {
        return ioReactor.getStatus() == IOReactorStatus.ACTIVE;
    }
}
