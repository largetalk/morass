/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public class MonitorPercentile {

    public static final String statisticID = "PercentSolutionID";
    public TimePercentileEnum enumPercentile;
    public long time;

    public MonitorPercentile(TimePercentileEnum enumPercentile,
            long time) {
        this.enumPercentile = enumPercentile;
        this.time = time;
    }
}
