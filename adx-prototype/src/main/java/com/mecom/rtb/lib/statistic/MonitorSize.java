/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public class MonitorSize {
    public static final String statisticID = "SizeSolutionID";

    private int width;
    private int height;

    public MonitorSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String monitorField() {
        return width + "X" + height;
    }
}
