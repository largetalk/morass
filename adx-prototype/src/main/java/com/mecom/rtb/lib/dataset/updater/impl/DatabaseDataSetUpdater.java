/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.updater.impl;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringArrayAnnotation;
import com.adsame.rtb.lib.dataset.DataSet;
import com.adsame.rtb.lib.dataset.RecordProcessor;
import com.adsame.rtb.lib.dataset.updater.DataSetUpdater;
import com.adsame.rtb.lib.util.SQLHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseDataSetUpdater implements DataSetUpdater {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DatabaseDataSetUpdater.class);

    private Section section;

    private Connection connection;

    private RecordProcessor recordProcessors[];

    private PreparedStatement deleteStatements[];
    private PreparedStatement insertStatements[];

    public DatabaseDataSetUpdater() {
    }

    @MetaAnnotation(
            prefix = "database-data-set-updater",
            comment = "database data set updater configuration")
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

        deleteStatements = new PreparedStatement[tableNameList.length];
        insertStatements = new PreparedStatement[tableNameList.length];
        for (int i = 0; i < tableNameList.length; i++) {
            String deleteSQL = SQLHelper.generateDeleteSQL(tableNameList[i]);
            deleteStatements[i] =
                    SQLHelper.prepareStatement(connection, deleteSQL);
            if (deleteStatements[i] == null) {
                String message = String.format(
                        "create delete statement for %s failed",
                        tableNameList[i]);
                throw new IllegalArgumentException(message);
            }

            String insertSQL = SQLHelper.generateInsertSQL(tableNameList[i],
                    recordProcessors[i].getFieldNames());
            insertStatements[i] =
                    SQLHelper.prepareStatement(connection, insertSQL);
            if (insertStatements[i] == null) {
                String message = String.format(
                        "create insert statement for %s failed",
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
    public synchronized void update(DataSet dataSet) {
        if (connection == null) {
            return;
        }
        dataSet.putToSQL(deleteStatements, insertStatements, recordProcessors);
    }

    @Override
    public synchronized void clear() {
        if (connection == null) {
            return;
        }
        for (int i = 0; i < deleteStatements.length; i++) {
            SQLHelper.executeUpdate(deleteStatements[i]);
        }
    }

    @Override
    public synchronized void close() {
        SQLHelper.closeConnection(connection);
        connection = null;
    }
}
