/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.client;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.http.common.OptionProvider;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.client.methods.HttpUriRequest;

public abstract class AbstractAsyncHttpClient
        implements Initializable, OptionProvider {

    protected int connectionTimeout;
    protected int socketTimeout;
    protected boolean socketKeepAlive;
    protected int socketBufferSize;
    protected boolean tcpNoDelay;

    private Section section;

    @MetaAnnotation(
            prefix = "abstract-async-http-client",
            comment = "abstract async http client configuration")
    public static final class Meta {

        @IntegerAnnotation(
                defaultValue = 1000,
                comment = "connection timeout of abstract async http client"
                          + "in milli-seconds")
        public static final String CONNECTION_TIMEOUT = "connectionTimeout";

        @IntegerAnnotation(
                defaultValue = 500,
                comment = "socket timeout of abstract async http client"
                          + "in milli-seconds")
        public static final String SOCKET_TIMEOUT = "socketTimeOut";

        @BooleanAnnotation(
                defaultValue = false,
                comment = "socket keep alive of abstract async http client")
        public static final String SOCKET_KEEP_ALIVE = "socketKeepAlive";

        @IntegerAnnotation(
                defaultValue = 8 * 1024,
                comment = "socket buffer size of abstract async http server"
                          + "in bytes")
        public static final String SOCKET_BUFFER_SIZE = "socketBufferSize";

        @BooleanAnnotation(
                defaultValue = true,
                comment = "tcp no delay of abstract async http client")
        public static final String TCP_NO_DELAY = "tcpNoDelay";
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        connectionTimeout = (Integer) section.get(Meta.CONNECTION_TIMEOUT);
        socketTimeout = (Integer) section.get(Meta.SOCKET_TIMEOUT);
        socketKeepAlive = (Boolean) section.get(Meta.SOCKET_KEEP_ALIVE);
        socketBufferSize = (Integer) section.get(Meta.SOCKET_BUFFER_SIZE);
        tcpNoDelay = (Boolean) section.get(Meta.TCP_NO_DELAY);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }

    @Override
    public Object getOption(String key) {
        if (section.contains(key)) {
            return section.get(key);
        } else {
            return null;
        }
    }

    @Override
    public void setOption(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public abstract AsyncHttpClientFuture execute(HttpUriRequest request,
            AsyncHttpClientCallback callback);

    public abstract void start();

    public abstract void stop();

    public abstract boolean isAlive();
}
