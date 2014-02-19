/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.client.impl;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.dataset.DataSet;
import com.adsame.rtb.lib.dataset.client.DataSetClient;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedDataSetClient implements DataSetClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CachedDataSetClient.class);

    private Section section;

    private DataSetClient wrappedClient;
    private int refreshInterval;

    private Lock wrappedClientProtector;
    private AtomicReference<DataSet> cachedDataSetReference;
    private InventoryRefreshTask dataSetRefreshTask;
    private Timer dataSetRefreshTimer;

    public CachedDataSetClient() {
    }

    @MetaAnnotation(
            prefix = "cached-data-set-client",
    comment = "cached data set client configuration")
    public static final class Meta {

        @StringAnnotation(
                comment = "name of wrapped client")
        public static final String WRAPPED_CLIENT_NAME = "wrappedClientName";

        @IntegerAnnotation(
                defaultValue = 60 * 1000,
        comment = "refresh interval of cache in milli-seconds")
        public static final String REFRESH_INTERVAL = "refreshInterval";
    }

    private class InventoryRefreshTask extends TimerTask {

        @Override
        public void run() {
            DataSet freshDataSet = null;
            wrappedClientProtector.lock();
            try {
                if (wrappedClient != null) {
                    freshDataSet = wrappedClient.retrieveDataSet();
                }
            } catch (Exception ex) {
                LOGGER.error("retrieve the data exception!", ex);
            } finally {
                wrappedClientProtector.unlock();
            }
            if (freshDataSet != null) {
                cachedDataSetReference.set(freshDataSet);
            }
        }
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        String wrappedClientName = (String) section.get(
                Meta.WRAPPED_CLIENT_NAME);
        wrappedClient = (DataSetClient) configuration.createInstance(
                wrappedClientName);
        if (wrappedClient == null) {
            LOGGER.error("wrapped client {} create failed", wrappedClientName);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        refreshInterval = (Integer) section.get(Meta.REFRESH_INTERVAL);

        wrappedClientProtector = new ReentrantLock();
        cachedDataSetReference = new AtomicReference<DataSet>(
                wrappedClient.retrieveDataSet());
        dataSetRefreshTask = new InventoryRefreshTask();
        dataSetRefreshTimer = new Timer();
        dataSetRefreshTimer.scheduleAtFixedRate(dataSetRefreshTask,
                refreshInterval, refreshInterval);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
        wrappedClient.saveConfigurationRecursively(outputStream, level + 1);
    }

    @Override
    public DataSet retrieveDataSet() {
        return cachedDataSetReference.get();
    }

    @Override
    public void close() {
        wrappedClientProtector.lock();
        try {
            if (wrappedClient != null) {
                dataSetRefreshTimer.cancel();
                wrappedClient.close();
                wrappedClient = null;
            }
        } finally {
            wrappedClientProtector.unlock();
        }
    }
}
