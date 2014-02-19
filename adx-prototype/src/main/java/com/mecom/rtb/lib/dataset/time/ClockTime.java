/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.time;

import com.adsame.rtb.lib.util.TimeUtility;
import java.util.Date;

public class ClockTime extends Date {

    public static final long ORIGIN =
            java.sql.Time.valueOf("00:00:00").getTime();

    public ClockTime(long timeInMilliSeconds) {
        super(timeInMilliSeconds);
    }

    public ClockTime(String value) {
        long timeInMilliSeconds = java.sql.Time.valueOf(value).getTime();
        super.setTime(timeInMilliSeconds);
    }

    public static ClockTime valueOf(String value) {
        return new ClockTime(value);
    }

    @Override
    public String toString() {
        long timeInMilliSeconds = getTime() - ORIGIN;
        long hour = timeInMilliSeconds / TimeUtility.MILLISECONDS_PER_HOUR;
        timeInMilliSeconds = timeInMilliSeconds
                - hour * TimeUtility.MILLISECONDS_PER_HOUR;
        long minute = timeInMilliSeconds / TimeUtility.MILLISECONDS_PER_MINUTE;
        timeInMilliSeconds = timeInMilliSeconds
                - minute * TimeUtility.MILLISECONDS_PER_MINUTE;
        long second = timeInMilliSeconds / TimeUtility.MILLISECONDS_PER_SECOND;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
