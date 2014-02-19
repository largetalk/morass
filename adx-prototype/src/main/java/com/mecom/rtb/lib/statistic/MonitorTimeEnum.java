/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public enum MonitorTimeEnum {

    CookieManager("time-cookieManager"),
    SplitTime("time-split"),
    RetargetingTime("time-retargeting"),
    FrequencyTime("time-frequency"),
    AdsameCookieGet("time-adsameCookieGet"),
    TotalTime("time-total"),
    FCApplyTime("time-apply"),
    FCCommitTime("time-commit"),
    FCReadMongoTime("time-readmongo");

    private final String value;

    private MonitorTimeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MonitorTimeEnum getValueOf(String value) {
        for (MonitorTimeEnum timeEnum : MonitorTimeEnum.values()) {
            if (value.equalsIgnoreCase(timeEnum.getValue())) {
                return timeEnum;
            }
        }
        return null;
    }
}
