/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.client.impl;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringArrayAnnotation;
import com.adsame.rtb.lib.dataset.DataSet;
import com.adsame.rtb.lib.dataset.DataSetProcessor;
import com.adsame.rtb.lib.dataset.RecordProcessor;
import com.adsame.rtb.lib.dataset.client.DataSetClient;
import com.adsame.rtb.lib.util.SQLHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseDataSetClient implements DataSetClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DatabaseDataSetClient.class);

    private Section section;

    private Connection connection;

    private RecordProcessor recordProcessors[];
    private DataSetProcessor dataSetProcessor;

    private PreparedStatement selectStatements[];

    public DatabaseDataSetClient() {
    }

    @MetaAnnotation(
            prefix = "database-data-set-client",
            comment = "database data set client configuration")
    public static final class Meta {

        @StringAnnotation(
                defaultValue = "localhost",
                comment = "host of database server")
        public static final String HOST = "host";

        @ShortAnnotation(
                defaultValue = 3306,
                comment = "port of database server")
        public static final String PORT = "port";

        @StringAnnotation(
                defaultValue = "sammix",
                comment = "database name of database server")
        public static final String DATABASE = "database";

        @StringAnnotation(
                comment = "username of database server")
        public static final String USERNAME = "username";

        @StringAnnotation(
                comment = "password of database server")
        public static final String PASSWORD = "password";

        @StringArrayAnnotation(
                comment = "table name list")
        public static final String TABLE_NAME_LIST = "tableNameList";

        @StringArrayAnnotation(
                comment = "record processor name list")
        public static final String RECORD_PROCESSOR_NAME_LIST =
                "recordProcessorNameList";

        @StringAnnotation(
                comment = "data set processor name")
        public static final String DATA_SET_PROCESSOR_NAME =
                "dataSetProcessorName";
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        String host = (String) section.get(Meta.HOST);
        short port = (Short) section.get(Meta.PORT);
        String database = (String) section.get(Meta.DATABASE);
        String username = (String) section.get(Meta.USERNAME);
        String password = (String) section.get(Meta.PASSWORD);
        String tableNameList[] = (String[]) section.get(Meta.TABLE_NAME_LIST);
        String recordProcessorNameList[] = (String[]) section.get(
                Meta.RECORD_PROCESSOR_NAME_LIST);
        String dataSetProcessorName = (String) section.get(
                Meta.DATA_SET_PROCESSOR_NAME);

        if (tableNameList.length != recordProcessorNameList.length) {
            throw new IllegalArgumentException("Illegal Configuration");
        }

        String url = SQLHelper.generateURL(host, port, database, true);
        connection = SQLHelper.openConnection(url, username, password);
        if (connection == null) {
            throw new IllegalArgumentException("Illegal Configuration");
        }

        recordProcessors = new RecordProcessor[recordProcessorNameList.length];
        for (int i = 0; i < recordProcessors.length; i++) {
            try {
                Class recordProcessorClass = Class.forName(
                        recordProcessorNameList[i]);
                recordProcessors[i] =
                        (RecordProcessor) recordProcessorClass.newInstance();
            } catch (Exception ex) {
                LOGGER.error("record processor class {} create failed",
                        recordProcessorNameList[i], ex);
                throw new IllegalArgumentException("Illegal Configuration");
            }
        }

        try {
            Class dataSetProcessorClass = Class.forName(dataSetProcessorName);
            dataSetProcessor =
                    (DataSetProcessor) dataSetProcessorClass.newInstance();
        } catch (Exception ex) {
            LOGGER.error("data set processor class {} create failed",
                        dataSetProcessorName, ex);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        selectStatements = new PreparedStatement[tableNameList.length];
        for (int i = 0; i < tableNameList.length; i++) {
            String selectSQL = SQLHelper.generateSelectSQL(tableNameList[i]);
            selectStatements[i] = SQLHelper.prepareStatement(connection,
                    selectSQL);
            if (selectStatements[i] == null) {
                String message = String.format(
                        "create select statement for %s failed",
                        tableNameList[i]);
                throw new IllegalArgumentException(message);
            }
        }
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }

    @Override
    public synchronized DataSet retrieveDataSet() {
        if (connection == null) {
            return null;
        }
        for (int i = 0; i < selectStatements.length; i++) {
            if (selectStatements[i] == null) {
                return null;
            }
        }
        DataSet dataSet = DataSet.getFromSQL(selectStatements,
                recordProcessors);
        dataSet = dataSetProcessor.process(dataSet);
        return dataSet;
    }

    @Override
    public synchronized void close() {
        SQLHelper.closeConnection(connection);
        connection = null;
    }
}
