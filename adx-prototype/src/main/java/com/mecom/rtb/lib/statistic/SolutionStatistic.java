/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

import java.math.BigDecimal;

public class SolutionStatistic {

    public static final String GLOBAL_SOLUTION_ID = "GlobalSolutionID";

    private double totalPrice;

    public String solutionID;
    public double averageCount;
    public long priceCount;
    public String monitorField;
    public long monitorCount;

    public static class PriceStatisticInfo {
        public final String solutionID;
        public final double price;

        public PriceStatisticInfo(String solutionID, double price) {
            this.solutionID = solutionID;
            this.price = price;
        }
    }

    public SolutionStatistic(String solutionID, String monitorField) {
        this.totalPrice = 0.0;
        this.solutionID = solutionID;
        this.monitorField = monitorField;
        this.monitorCount = 0L;
        this.priceCount = 0L;
        this.averageCount = 0.0;
    }

    public SolutionStatistic(String solutionID, String monitorField,
            long monitorCount) {
        this.totalPrice = 0.0;
        this.solutionID = solutionID;
        this.monitorField = monitorField;
        this.monitorCount = monitorCount;
        this.priceCount = 0L;
        this.averageCount = 0.0;
    }

    public synchronized void addFieldCountByOne() {
        monitorCount += 1;
    }

    public synchronized void addFieldCountByValue(long monitorValue) {
        monitorCount += monitorValue;
    }

    public synchronized void addTotalPrice(double price) {
        totalPrice += price;
        priceCount += 1;
        averageCount =  totalPrice / priceCount;
        BigDecimal b = new BigDecimal(averageCount);
        averageCount = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
