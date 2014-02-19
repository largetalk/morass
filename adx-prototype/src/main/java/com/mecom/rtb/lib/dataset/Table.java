/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset;

import com.adsame.rtb.lib.util.SQLHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table<R extends Record> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Table.class);

    private ArrayList<R> recordList;

    public Table(ArrayList recordList) {
        this.recordList = recordList;
    }

    public static <R extends Record> Table readFromCSVForUnitTest(
            RecordProcessor<R> recordProcessor, File file) {
        Table table = null;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            CSVParser parser = new CSVParser(reader);
            String valueArrays[][] = parser.getAllValues();

            String titleNames[] = valueArrays[0];
            HashMap<String, Integer> indexMap = createIndexMap(titleNames);

            ArrayList<R> recordList = new ArrayList<R>();
            // i starts from 1 to skip the title line
            for (int i = 1; i < valueArrays.length; i++) {
                String values[] = valueArrays[i];
                R record = recordProcessor.parseFrom(values, indexMap);
                if (record != null) {
                    recordList.add(record);
                }
            }

            table = new Table(recordList);
        } catch (FileNotFoundException ex) {
            LOGGER.error("csv file {} read error", file.getPath(), ex);
        } catch (IOException ex) {
            LOGGER.error("csv file {} parse error", file.getPath(), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.error("csv file {} reader close error",
                            file.getPath(), ex);
                }
            }
        }

        return table;
    }

    public static <R extends Record> Table getFromSQL(
            RecordProcessor<R> recordProcessor,
            PreparedStatement selectStatement) {
        Table table = null;

        ResultSet resultSet = SQLHelper.executeQuery(selectStatement);
        if (resultSet == null) {
            return table;
        }

        try {
            ArrayList<R> recordList = new ArrayList<R>();
            while (resultSet.next()) {
                R record = recordProcessor.parseFrom(resultSet);
                if (record != null && record.isValid()) {
                    recordList.add(record);
                }
            }
            table = new Table(recordList);
        } catch (SQLException ex) {
            LOGGER.error("sql select operation {} result parse error",
                    selectStatement, ex);
        }

        return table;
    }

    public <R extends Record> void writeToCSV(
            RecordProcessor<R> recordProcessor, File file) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            CSVPrinter printer = new CSVPrinter(
                    bufferedWriter, CSVStrategy.DEFAULT_STRATEGY);

            printer.println(recordProcessor.getFieldNames());
            for (int i = 0; i < getNumRecords(); i++) {
                R record = (R) getRecord(i);
                String values[] = recordProcessor.valuesToStringArray(record);
                printer.println(values);
            }
        } catch (IOException ex) {
            LOGGER.error("csv file {} write error", file.getPath(), ex);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ex) {
                    LOGGER.error("csv file {} writer close error",
                            file.getPath(), ex);
                }
            }
        }
    }

    public <R extends Record> void putToSQL(
            RecordProcessor<R> recordProcessor,
            PreparedStatement deleteStatement,
            PreparedStatement insertStatement) {
        SQLHelper.executeUpdate(deleteStatement);

        for (int i = 0; i < getNumRecords(); i++) {
            R record = (R) getRecord(i);
            recordProcessor.setStatement(record, insertStatement);
            SQLHelper.executeUpdate(insertStatement);
        }
    }

    public <R extends Record> Table duplicate(
            RecordProcessor<R> recordProcessor) {
        ArrayList<R> newRecordList = new ArrayList<R>();
        for (int i = 0; i < getNumRecords(); i++) {
            R record = (R) getRecord(i);
            newRecordList.add(recordProcessor.duplicate(record));
        }
        return new Table(newRecordList);
    }

    public int getNumRecords() {
        return recordList.size();
    }

    public R getRecord(int index) {
        return recordList.get(index);
    }

    // create a index map from fieldNames to their column indexes
    private static HashMap<String, Integer> createIndexMap(
            String titleNames[]) {
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
        for (int i = 0; i < titleNames.length; i++) {
            indexMap.put(titleNames[i], i);
        }
        return indexMap;
    }
}
