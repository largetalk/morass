/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.httpserver.async;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
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
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncHttpServer implements Initializable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AsyncHttpServer.class);

    private Section section;

    private short port;
    private int socketTimeout;
    private boolean socketKeepAlive;
    private int socketBufferSize;
    private boolean tcpNoDelay;
    private int numIOThreads;
    private String handlerPattern;
    private ConnectionReuseStrategy connectionReuseStrategy;
    private AsyncRequestHandler requestHandler;

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

        @ShortAnnotation(
                defaultValue = 8080,
                comment = "port of async http server")
        public static final String PORT = "port";

        @IntegerAnnotation(
                defaultValue = 500,
                comment = "socket timeout of async http server"
                          + "in milli-seconds")
        public static final String SOCKET_TIMEOUT = "socketTimeOut";

        @BooleanAnnotation(
                defaultValue = false,
                comment = "socket keep alive of async http server")
        public static final String SOCKET_KEEP_ALIVE = "socketKeepAlive";

        @IntegerAnnotation(
                defaultValue = 8 * 1024,
                comment = "socket buffer size of async http server in bytes")
        public static final String SOCKET_BUFFER_SIZE = "socketBufferSize";

        @BooleanAnnotation(
                defaultValue = true,
                comment = "tcp no delay of async http server")
        public static final String TCP_NO_DELAY = "tcpNoDelay";

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

        private final short port;
        private final IOEventDispatch ioEventDispatch;
        private final ListeningIOReactor ioReactor;

        public ServerThread(short port, IOEventDispatch ioEventDispatch,
                ListeningIOReactor ioReactor) {
            this.port = port;
            this.ioEventDispatch = ioEventDispatch;
            this.ioReactor = ioReactor;
        }

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
        section = configuration.getSection(Meta.class);

        port = (Short) section.get(Meta.PORT);
        socketTimeout = (Integer) section.get(Meta.SOCKET_TIMEOUT);
        socketKeepAlive = (Boolean) section.get(Meta.SOCKET_KEEP_ALIVE);
        socketBufferSize = (Integer) section.get(Meta.SOCKET_BUFFER_SIZE);
        tcpNoDelay = (Boolean) section.get(Meta.TCP_NO_DELAY);
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

        String requestHandlerName = (String) section.get(
                Meta.REQUEST_HANDLER_NAME);
        requestHandler = (AsyncRequestHandler) configuration.createInstance(
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
        requestHandler.saveConfigurationRecursively(outputStream, level + 1);
    }

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
        registry.register(handlerPattern, requestHandler);

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

        serverThread = new ServerThread(port, ioEventDispatch, ioReactor);
        serverThread.start();
    }

    public void stop() throws IOException {
        ioReactor.shutdown();
        requestHandler.dispose();
    }

    public boolean isAlive() {
        return serverThread.isAlive();
    }
}
