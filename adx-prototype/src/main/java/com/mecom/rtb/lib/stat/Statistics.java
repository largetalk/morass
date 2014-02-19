/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.stat;

import com.adsame.rtb.lib.util.ArraySearcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// This class is used to calculate common statistics of real scalars.
//
// Users can add arbitrary number of scalars at arbitrary time
// and get up-to-date statistics of those scalars.
//
// To improve the performance, we adopt lazy evaluation strategy.
// If you need to modify the code, please be aware of it!
public class Statistics {

    private ArrayList<Double> list;
    private ArrayList<Double> tempList;

    private double mean;
    private double variance;
    private double standardDeviation;
    private double max;
    private double min;
    private double median;

    public Statistics() {
        list = new ArrayList();
        tempList = new ArrayList();

        mean = 0.0;
        variance = 0.0;
        standardDeviation = 0.0;
        max = 0.0;
        min = 0.0;
        median = 0.0;
    }

    public synchronized boolean isUpdated() {
        return tempList.isEmpty();
    }

    public synchronized <T extends Number> void add(T data) {
        tempList.add(data.doubleValue());
    }

    public synchronized <T extends Number> void add(List<T> dataList) {
        for (T data : dataList) {
            tempList.add(data.doubleValue());
        }
    }

    public synchronized int getSize() {
        update();
        return list.size();
    }

    public synchronized ArrayList<Double> getList() {
        update();
        return new ArrayList<Double>(list);
    }

    public synchronized double getMean() {
        update();
        return mean;
    }

    public synchronized double getVariance() {
        update();
        return variance;
    }

    public synchronized double getStandardDeviation() {
        update();
        return standardDeviation;
    }

    public synchronized double getMax() {
        update();
        return max;
    }

    public synchronized double getMin() {
        update();
        return min;
    }

    public synchronized double getMedian() {
        update();
        return median;
    }

    // get number of elements in [min, max)
    public synchronized int getNumInRange(Double min, Double max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must < max");
        }

        update();
        int leftBoundOfMax = ArraySearcher.locateLeftBound(list, max);
        int leftBoundOfMin = ArraySearcher.locateLeftBound(list, min);
        return leftBoundOfMax - leftBoundOfMin;
    }

    @Override
    public synchronized String toString() {
        update();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Mean : ");
        stringBuilder.append(getMean()).append("\n");
        stringBuilder.append("Variance : ");
        stringBuilder.append(getVariance()).append("\n");
        stringBuilder.append("Standard Deviation : ");
        stringBuilder.append(getStandardDeviation()).append("\n");
        stringBuilder.append("Max : ");
        stringBuilder.append(getMax()).append("\n");
        stringBuilder.append("Min : ");
        stringBuilder.append(getMin()).append("\n");
        stringBuilder.append("Median : ");
        stringBuilder.append(getMedian());
        return stringBuilder.toString();
    }

    private static double calculateMean(ArrayList<Double> list) {
        int size = list.size();
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum += list.get(i);
        }
        return sum / size;
    }

    private static double calculateVariance(ArrayList<Double> list,
            double mean) {
        int size = list.size();
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            double difference = mean - list.get(i);
            sum += difference * difference;
        }
        return sum / size;
    }

    private static double calculateStandardDeviation(double variance) {
        return Math.sqrt(variance);
    }

    private static double calculateMax(ArrayList<Double> list) {
        return list.get(list.size() - 1);
    }

    private static double calculateMin(ArrayList<Double> list) {
        return list.get(0);
    }

    private static double calculateMedian(ArrayList<Double> list) {
        int size = list.size();
        int medianIndex = size / 2;
        if ((size % 2) == 1) {
            return list.get(medianIndex);
        } else {
            return (list.get(medianIndex - 1) + list.get(medianIndex)) / 2;
        }
    }

    private static ArrayList<Double> mergeList(List<Double> list,
            List<Double> tempList) {
        ArrayList<Double> mergedList = new ArrayList<Double>();

        // list is always sorted
        Double array[] = list.toArray(new Double[0]);

        Double tempArray[] = tempList.toArray(new Double[0]);
        Arrays.sort(tempArray);

        int dataIndex = 0;
        int tempDataIndex = 0;
        while (dataIndex < array.length
                && tempDataIndex < tempArray.length) {
            Double data = array[dataIndex];
            Double tempData = tempArray[tempDataIndex];
            if (data.compareTo(tempData) <= 0) {
                mergedList.add(data);
                dataIndex += 1;
            } else {
                mergedList.add(tempData);
                tempDataIndex += 1;
            }
        }
        while (dataIndex < array.length) {
            Double data = array[dataIndex];
            mergedList.add(data);
            dataIndex += 1;
        }
        while (tempDataIndex < tempArray.length) {
            Double tempData = tempArray[tempDataIndex];
            mergedList.add(tempData);
            tempDataIndex += 1;
        }

        return mergedList;
    }

    private void update() {
        if (!tempList.isEmpty()) {
            list = mergeList(list, tempList);
            tempList.clear();
            mean = calculateMean(list);
            variance = calculateVariance(list, mean);
            standardDeviation = calculateStandardDeviation(variance);
            max = calculateMax(list);
            min = calculateMin(list);
            median = calculateMedian(list);
        }
    }
}
