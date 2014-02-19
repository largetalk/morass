/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Creates sync thrift client to a fixed remote host
public class ThriftSyncClientFactory<T extends TServiceClient>
        implements ObjectFactory<T, Object>, Initializable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ThriftSyncClientFactory.class);

    private Section section;

    private TServiceClientFactory<T> thriftFactory;
    private String host;
    private Integer port;
    private Integer timeout;
    private boolean isFramedTransport;

    public ThriftSyncClientFactory(TServiceClientFactory<T> thriftFactory) {
        this(thriftFactory, null, null, null, true);
    }

    public ThriftSyncClientFactory(TServiceClientFactory<T> thriftFactory,
            String host, Integer port, Integer timeout) {
        this(thriftFactory, host, port, timeout, true);
    }

    public ThriftSyncClientFactory(TServiceClientFactory<T> thriftFactory,
            String host, Integer port,
            Integer timeout, boolean isFramedTransport) {
        this.thriftFactory = thriftFactory;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.isFramedTransport = isFramedTransport;
    }


    @MetaAnnotation(
            prefix = "thrift-sync-client",
            comment = "thrift sync client configuration")
    public static final class Meta {

        @StringAnnotation(
                comment = "remote host")
        public static final String HOST = "host";

        @IntegerAnnotation(
                comment = "remote port")
        public static final String PORT = "port";

        @IntegerAnnotation(
                defaultValue = 30,
                comment = "connect/receive timeout in milli seconds")
        public static final String TIMEOUT = "timeout";

        @BooleanAnnotation(
                defaultValue = true,
                comment = "framed transport")
        public static final String FRAMED_TRANSPORT = "framedTransport";
    }

    @Override
    public T createObject(Object parameter) {
        TSocket socket = new TSocket(host, port, timeout);
        TTransport transport = socket;
        if (isFramedTransport) {
            transport = new TFramedTransport(socket);
        }
        try {
            transport.open();
        } catch (TTransportException ex) {
            LOGGER.error("connect to {}:{} failed",
                    new Object[]{host, port, ex});
            return null;
        }

        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
        TProtocol protocol = protocolFactory.getProtocol(transport);
        return thriftFactory.getClient(protocol);
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);
        host = (String) section.get(Meta.HOST);
        port = (Integer) section.get(Meta.PORT);
        timeout = (Integer) section.get(Meta.TIMEOUT);
        isFramedTransport = (Boolean) section.get(Meta.FRAMED_TRANSPORT);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }
}