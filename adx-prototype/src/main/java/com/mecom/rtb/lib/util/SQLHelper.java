/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SQLHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SQLHelper.class);

    public static String generateURL(String host, short port,
            String database, boolean usesAutoReconnect) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        if (usesAutoReconnect == true) {
            url = url + "?autoReconnect=true";
        }
        return url;
    }

    public static Connection openConnection(
            String url, String user, String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception ex) {
            LOGGER.error("sql connection {}:{}:{} failed",
                    new Object[]{url, user, password, ex});
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            LOGGER.error("sql connection close failed", ex);
        }
    }

    public static String generateSelectSQL(String table) {
        return "select * from `" + table + "`";
    }

    public static String generateInsertSQL(String table, String fieldNames[]) {
        for (int i = 0; i < fieldNames.length; i++) {
            fieldNames[i] = "`" + fieldNames[i] + "`";
        }
        String fields = StringUtils.join(fieldNames, ",");
        String values = StringUtils.repeat("?", ",", fieldNames.length);
        String sql = String.format("insert into `%s` (%s) values (%s)",
                table, fields, values);
        return sql;
    }

    public static String generateDeleteSQL(String table) {
        return "delete from `" + table + "`";
    }

    public static PreparedStatement prepareStatement(
            Connection connection, String textSQL) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(textSQL);
        } catch (Exception ex) {
            LOGGER.error("sql statement {} preparation failed", textSQL, ex);
        }
        return statement;
    }

    public static ResultSet executeQuery(PreparedStatement statement) {
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery();
        } catch (SQLException ex) {
            LOGGER.error("sql query operation {} error", statement, ex);
        }
        return resultSet;
    }

    public static int executeUpdate(PreparedStatement statement) {
        int result = 0;
        try {
            result = statement.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("sql update operation {} error", statement, ex);
        }
        return result;
    }
}
