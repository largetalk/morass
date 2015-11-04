/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.http.core;

import org.apache.commons.lang.StringEscapeUtils;


public class SqlEscape {

    public static void main(String args[]) {
        String a = "xxx'yyy";
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("'");
        sqlBuilder.append(StringEscapeUtils.escapeSql(a));
        sqlBuilder.append("'");
        System.out.println(sqlBuilder.toString());

         String b = "xxx\\yyy";
        StringBuilder sqlBuilder1 = new StringBuilder();
        sqlBuilder1.append("'");
        sqlBuilder1.append(StringEscapeUtils.escapeSql(b));
        sqlBuilder1.append("'");
        System.out.println(sqlBuilder1.toString());
        
        System.out.println(StringEscapeUtils.escapeJava(a));
        System.out.println(StringEscapeUtils.escapeJava(b));
        
    }
}
