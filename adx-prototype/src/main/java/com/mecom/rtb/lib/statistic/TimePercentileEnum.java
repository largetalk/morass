/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public enum TimePercentileEnum {

    CookieManagerPercentile("time-cookieManager-percentile"),
    SplitTimePercentile("time-split-percentile"),
    RetargetingTimePercentile("time-retargeting-percentile"),
    FrequencyTimePercentile("time-frequency-percentile"),
    AdsameCookieGetPercentile("time-adsameCookieGet-percentile"),
    TotalTimePercentile("time-total-percentile"),
    FCApplyPercentile("time-apply-percentile"),
    FCCommitPercentile("time-commit-percentile"),
    FCReadMongoPercentile("time-readmongo-percentile");

    private final String value;

    private TimePercentileEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
