/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.httpserver.sync;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncHttpServer implements Initializable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SyncHttpServer.class);

    private Section section;

    private Set<HttpServerConnection> connectionSet;

    private short port;
    private int numMaxConnections;
    private int socketTimeout;
    private boolean socketKeepAlive;
    private int socketBufferSize;
    private boolean tcpNoDelay;
    private String handlerPattern;
    private ConnectionReuseStrategy connectionReuseStrategy;
    private SyncRequestHandler requestHandler;

    private ServerThread serverThread;

    public SyncHttpServer() {
    }

    @MetaAnnotation(
            prefix = "sync-http-server",
            comment = "sync http server configuration")
    public static final class Meta {

        @ShortAnnotation(
                defaultValue = 8080,
                comment = "port of sync http server")
        public static final String PORT = "port";

        @IntegerAnnotation(
                defaultValue = 10,
                min = 1,
                comment = "number of max connections of sync http server")
        public static final String NUM_MAX_CONNECTIONS = "numMaxConnections";

        @IntegerAnnotation(
                defaultValue = 500,
                comment = "socket timeout of sync http server in milli-seconds")
        public static final String SOCKET_TIMEOUT = "socketTimeOut";

        @BooleanAnnotation(
                defaultValue = false,
                comment = "socket keep alive of sync http server")
        public static final String SOCKET_KEEP_ALIVE = "socketKeepAlive";

        @IntegerAnnotation(
                defaultValue = 8 * 1024,
                comment = "socket buffer size of sync http server in bytes")
        public static final String SOCKET_BUFFER_SIZE = "socketBufferSize";

        @BooleanAnnotation(
                defaultValue = true,
                comment = "tcp no delay of sync http server")
        public static final String TCP_NO_DELAY = "tcpNoDelay";

        @StringAnnotation(
                defaultValue = "*",
                comment = "handler pattern of sync http server")
        public static final String HANDLER_PATTERN = "handlerPattern";

        @StringAnnotation(
                comment = "name of connection reuse strategy")
        public static final String CONNECTION_REUSE_STRATEGY_NAME =
                "connectionReuseStrategyName";

        @StringAnnotation(
                comment = "name of request handler")
        public static final String REQUEST_HANDLER_NAME = "requestHandlerName";
    }

    private class ServerThread extends Thread {

        private final HttpService httpService;

        private final ExecutorService threadPool;

        private ServerSocket serverSocket;

        public ServerThread(HttpService httpService) {
            this.httpService = httpService;

            threadPool = Executors.newFixedThreadPool(numMaxConnections);
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
            } catch (IOException ex) {
                LOGGER.error("server socket create failed", ex);
                return;
            }

            try {
                while (!Thread.interrupted()) {
                    // Set up HTTP connection
                    Socket socket = serverSocket.accept();
                    if (getNumConnections() >= numMaxConnections) {
                        socket.close();
                        LOGGER.warn("connection number exceeds limitation");
                    } else {
                        HttpServerConnection httpConnection =
                                openConnection(socket);

                        // Dispatch worker thread
                        Thread workerThread = new WorkerThread(httpService,
                                httpConnection);
                        threadPool.submit(workerThread);
                    }
                }
            } catch (Exception ex) {
                LOGGER.info("server exception", ex);
            } finally {
                threadPool.shutdown();
            }
        }
    }

    private class WorkerThread extends Thread {

        private final HttpService httpService;
        private final HttpServerConnection connection;

        public WorkerThread(HttpService httpService,
                HttpServerConnection connection) {
            this.httpService = httpService;
            this.connection = connection;
        }

        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && connection.isOpen()) {
                    httpService.handleRequest(connection, context);
                }
            } catch (Exception ex) {
                LOGGER.info("worker exception", ex);
            } finally {
                closeConnection(connection);
            }
        }
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        connectionSet = new HashSet<HttpServerConnection>();

        port = (Short) section.get(Meta.PORT);
        numMaxConnections = (Integer) section.get(Meta.NUM_MAX_CONNECTIONS);
        socketTimeout = (Integer) section.get(Meta.SOCKET_TIMEOUT);
        socketKeepAlive = (Boolean) section.get(Meta.SOCKET_KEEP_ALIVE);
        socketBufferSize = (Integer) section.get(Meta.SOCKET_BUFFER_SIZE);
        tcpNoDelay = (Boolean) section.get(Meta.TCP_NO_DELAY);
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
        requestHandler = (SyncRequestHandler) configuration.createInstance(
                requestHandlerName);
        if (requestHandler == null) {
            LOGGER.error("request handler {} create failed",
                    requestHandlerName);
            throw new IllegalArgumentException("Illegal Configuration");
        }
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
        requestHandler.saveConfigurationRecursively(outputStream, level + 1);
    }

    public void start() {
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer())
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();

        // Set up request handlers
        UriHttpRequestHandlerMapper registry =
                new UriHttpRequestHandlerMapper();
        registry.register(handlerPattern, requestHandler);

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc,
                connectionReuseStrategy, null, registry);

        serverThread = new ServerThread(httpService);
        serverThread.start();
    }

    public void stop() throws IOException {
        serverThread.serverSocket.close();
        serverThread.threadPool.shutdown();
        closeAllConnections();
        requestHandler.dispose();
    }

    public boolean isAlive() {
        return serverThread.isAlive();
    }

    private synchronized int getNumConnections() {
        return connectionSet.size();
    }

    private synchronized HttpServerConnection openConnection(
            Socket socket) throws IOException {
        socket.setSoTimeout(socketTimeout);
        socket.setKeepAlive(socketKeepAlive);
        socket.setSendBufferSize(socketBufferSize);
        socket.setReceiveBufferSize(socketBufferSize);
        socket.setTcpNoDelay(tcpNoDelay);
        DefaultBHttpServerConnectionFactory connectionFactory =
                DefaultBHttpServerConnectionFactory.INSTANCE;
        DefaultBHttpServerConnection connection =
                connectionFactory.createConnection(socket);
        connection.bind(socket);
        connectionSet.add(connection);
        int numCurrentConnections = getNumConnections();
        LOGGER.info("connection opened - {} - {}", connection,
                numCurrentConnections);
        return connection;
    }

    private synchronized void closeConnection(
            HttpServerConnection connection) {
        if (connection.isOpen()) {
            connectionSet.remove(connection);
            int numCurrentConnections = getNumConnections();
            try {
                connection.shutdown();
                LOGGER.info("connection closed - {} - {}", connection,
                        numCurrentConnections);
            } catch (IOException ex) {
                LOGGER.error("connection shutdown failed", ex);
            }
        }
    }

    private synchronized void closeAllConnections() {
        HttpServerConnection connectionArray[] =
                connectionSet.toArray(new HttpServerConnection[0]);
        for (int i = 0; i < connectionArray.length; i++) {
            closeConnection(connectionArray[i]);
        }
    }
}
