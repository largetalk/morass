/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.stat;

import java.text.NumberFormat;
import java.util.ArrayList;

public class Histogram {

    private ArrayList<Column> columns;

    private Histogram(ArrayList<Column> columns) {
        this.columns = columns;
    }

    public static class Column {

        private double min;
        private double max;
        private int count;
        private double percent;

        private Column(double min, double max, int count, double percent) {
            this.min = min;
            this.max = max;
            this.count = count;
            this.percent = percent;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getCount() {
            return count;
        }

        public double getPercent() {
            return percent;
        }

        @Override
        public String toString() {
            NumberFormat format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Min = ").append(min);
            stringBuilder.append(", Max = ").append(max);
            stringBuilder.append(", Count = ").append(count);
            stringBuilder.append(", Percent = ").append(format.format(percent));
            return stringBuilder.toString();
        }
    }

    public static Histogram createInstance(Statistics statistics,
            double rangeFactor, int numMainColumns) {
        if (statistics == null) {
            throw new IllegalArgumentException("statistics must not be null");
        }
        if (rangeFactor <= 0) {
            throw new IllegalArgumentException("range factor must > 0");
        }
        if (numMainColumns <= 0) {
            throw new IllegalArgumentException("main bucket number must > 0");
        }

        ArrayList<Column> columns = new ArrayList<Column>();

        // Adjust range of main columns
        double mainColumnsMin = Math.max(statistics.getMin(),
                statistics.getMean()
                - rangeFactor * statistics.getStandardDeviation());
        double mainColumnsMax = Math.min(statistics.getMax(),
                statistics.getMean()
                + rangeFactor * statistics.getStandardDeviation());

        int totalCount = statistics.getSize();
        double percentFactor = 100.0 / totalCount;

        // Add head column
        double headColumnMin = Double.NEGATIVE_INFINITY;
        double headColumnMax = mainColumnsMin;
        int headColumnCount = statistics.getNumInRange(headColumnMin,
                headColumnMax);
        Column headColumn = new Column(headColumnMin, headColumnMax,
                headColumnCount, headColumnCount * percentFactor);
        columns.add(headColumn);

        // Add main columns
        double columnWidth = (mainColumnsMax - mainColumnsMin)
                / numMainColumns;
        double columnMin = mainColumnsMin;
        double columnMax = columnMin + columnWidth;
        for (int i = 0; i < numMainColumns; i++) {
            int columnCount = statistics.getNumInRange(columnMin, columnMax);
            Column column = new Column(columnMin, columnMax, columnCount,
                    columnCount * percentFactor);
            columns.add(column);
            columnMin = columnMax;
            columnMax = columnMin + columnWidth;
        }

        // Add tail columns
        double tailColumnMin = mainColumnsMax;
        double tailColumnMax = Double.POSITIVE_INFINITY;
        int tailColumnCount = statistics.getNumInRange(tailColumnMin,
                tailColumnMax);
        Column tailColumn = new Column(tailColumnMin, tailColumnMax,
                tailColumnCount, tailColumnCount * percentFactor);
        columns.add(tailColumn);

        return new Histogram(columns);
    }

    public int getNumColumns() {
        return columns.size();
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(columns.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }
}
