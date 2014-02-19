/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.time;

import java.sql.Timestamp;
import java.util.Date;

public class DateTime extends Date {

    public DateTime() {
        super();
    }

    public DateTime(long timeInMilliSeconds) {
        super(timeInMilliSeconds);
    }

    public static DateTime valueOf(String value) {
        long timestamp = Timestamp.valueOf(value).getTime();
        return new DateTime(timestamp);
    }

    @Override
    public String toString() {
        Timestamp timestamp = new Timestamp(getTime());
        return timestamp.toString();
    }
}
