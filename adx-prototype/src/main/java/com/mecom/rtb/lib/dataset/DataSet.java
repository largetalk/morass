/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset;

import java.io.File;
import java.sql.PreparedStatement;

public class DataSet {

    protected final Table tables[];

    public DataSet(Table tables[]) {
        this.tables = tables;
    }

    public static DataSet readFromCSVForUnitTest(File files[],
            RecordProcessor recordProcessors[]) {
        Table tables[] = new Table[files.length];
        for (int i = 0; i < tables.length; i++) {
            tables[i] = Table.readFromCSVForUnitTest(
                    recordProcessors[i], files[i]);
            if (tables[i] == null) {
                throw new IllegalArgumentException("read from CSV failed");
            }
        }
        return new DataSet(tables);
    }

    public static DataSet getFromSQL(PreparedStatement selectStatements[],
            RecordProcessor recordProcessors[]) {
        Table tables[] = new Table[selectStatements.length];
        for (int i = 0; i < tables.length; i++) {
            tables[i] = Table.getFromSQL(recordProcessors[i],
                    selectStatements[i]);
            if (tables[i] == null) {
                throw new IllegalArgumentException("get from SQL failed");
            }
        }
        return new DataSet(tables);
    }

    public void writeToCSV(File files[], RecordProcessor recordProcessors[]) {
        if (files.length != tables.length) {
            throw new IllegalArgumentException(
                    "diffenrent number of tables and files");
        }
        for (int i = 0; i < tables.length; i++) {
            tables[i].writeToCSV(recordProcessors[i], files[i]);
        }
    }

    public void putToSQL(PreparedStatement deleteStatements[],
            PreparedStatement insertStatements[],
            RecordProcessor recordProcessors[]) {
        if (deleteStatements.length != tables.length) {
            throw new IllegalArgumentException(
                    "diffenrent number of tables and delete statements");
        }
        if (insertStatements.length != tables.length) {
            throw new IllegalArgumentException(
                    "diffenrent number of tables and insert statements");
        }
        for (int i = 0; i < tables.length; i++) {
            tables[i].putToSQL(recordProcessors[i], deleteStatements[i],
                    insertStatements[i]);
        }
    }

    public int getNumTables() {
        return tables.length;
    }

    public Table getTable(int index) {
        return tables[index];
    }
}
