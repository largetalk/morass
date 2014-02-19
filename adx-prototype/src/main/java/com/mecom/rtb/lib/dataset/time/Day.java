/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.time;

import java.util.Date;

public class Day extends Date {

    public Day(long timeInMilliSeconds) {
       super(timeInMilliSeconds);
    }

    public static Day valueOf(String value) {
        java.sql.Date date = java.sql.Date.valueOf(value);
        return new Day(date.getTime());
    }

    @Override
    public String toString() {
        java.sql.Date date = new java.sql.Date(getTime());
        return date.toString();
    }
}
