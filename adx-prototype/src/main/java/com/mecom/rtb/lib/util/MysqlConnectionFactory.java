/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.ShortAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

public class MysqlConnectionFactory
        implements ObjectFactory<Connection, Object>, Initializable {

    private Section section;

    private String host;
    private Short port;
    private String database;
    private String username;
    private String password;

    public MysqlConnectionFactory() {
        this(null, null, null, null, null);
    }

    public MysqlConnectionFactory(String host, Short port, String database,
            String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @MetaAnnotation(
            prefix = "mysql-connection-factory",
            comment = "mysql connection factory configuration")
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
    }

    @Override
    public Connection createObject(Object parameter) {
        String url = SQLHelper.generateURL(host, port, database, true);
        Connection connection =
                SQLHelper.openConnection(url, username, password);
        return connection;
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);
        host = (String) section.get(Meta.HOST);
        port = (Short) section.get(Meta.PORT);
        database = (String) section.get(Meta.DATABASE);
        username = (String) section.get(Meta.USERNAME);
        password = (String) section.get(Meta.PASSWORD);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }
}
