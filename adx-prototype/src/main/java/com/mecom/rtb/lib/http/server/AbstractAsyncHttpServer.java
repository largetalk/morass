/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.server;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.http.common.OptionProvider;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractAsyncHttpServer
        implements Initializable, OptionProvider {

    private Section section;

    protected short port;
    protected int socketTimeout;
    protected boolean socketKeepAlive;
    protected int socketBufferSize;
    protected boolean tcpNoDelay;

    @MetaAnnotation(
            prefix = "abstract-async-http-server",
            comment = "async http server common configuration")
    public static final class Meta {

        @ShortAnnotation(
                comment = "port of async http server")
        public static final String PORT = "port";

        @IntegerAnnotation(
                defaultValue = 500,
                comment = "socket timeout of async http server"
                          + "in milli-seconds")
        public static final String SOCKET_TIMEOUT = "socketTimeOut";

        @BooleanAnnotation(
                defaultValue = true,
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
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        port = (Short) section.get(Meta.PORT);
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

    public abstract void start() throws IOException;

    public abstract void stop() throws IOException;

    public abstract boolean isAlive();
}
