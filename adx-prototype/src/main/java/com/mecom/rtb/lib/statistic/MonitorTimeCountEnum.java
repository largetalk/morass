/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public enum MonitorTimeCountEnum {

    CookieManagerCount("time-cookieManager-count"),
    SplitTimeCount("time-split-count"),
    RetargetingTimeCount("time-retargeting-count"),
    FrequencyTimeCount("time-frequency-count"),
    AdsameCookieGetCount("time-adsameCookieGet-count"),
    TotalTimeCount("time-total-count"),
    FCApplyCount("time-apply-count"),
    FCCommitCount("time-commit-count"),
    FCReadMongoCount("time-readmongo-count");

    private final String value;

    private MonitorTimeCountEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
