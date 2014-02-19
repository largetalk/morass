/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

import java.util.Map;
import java.util.HashMap;

public class MonitorTime {
    public static final String statisticID = "TimeSolutionID";
    public MonitorTimeEnum enumMonitorTime;
    public MonitorTimeCountEnum enumMonitorTimeCount;
    public TimePercentileEnum enumPercentile;
    public long time;

    private static Map<MonitorTimeEnum, MonitorTimeCountEnum> timeCountEnumMap;
    private static Map<MonitorTimeEnum, TimePercentileEnum> timePercentileMap;

    static {
        timeCountEnumMap = new HashMap<MonitorTimeEnum, MonitorTimeCountEnum>() {{
                    put(MonitorTimeEnum.CookieManager,
                            MonitorTimeCountEnum.CookieManagerCount);
                    put(MonitorTimeEnum.SplitTime,
                            MonitorTimeCountEnum.SplitTimeCount);
                    put(MonitorTimeEnum.RetargetingTime,
                            MonitorTimeCountEnum.RetargetingTimeCount);
                    put(MonitorTimeEnum.FrequencyTime,
                            MonitorTimeCountEnum.FrequencyTimeCount);
                    put(MonitorTimeEnum.AdsameCookieGet,
                            MonitorTimeCountEnum.AdsameCookieGetCount);
                    put(MonitorTimeEnum.TotalTime,
                            MonitorTimeCountEnum.TotalTimeCount);
                    put(MonitorTimeEnum.FCApplyTime,
                            MonitorTimeCountEnum.FCApplyCount);
                    put(MonitorTimeEnum.FCCommitTime,
                            MonitorTimeCountEnum.FCCommitCount);
                    put(MonitorTimeEnum.FCReadMongoTime,
                            MonitorTimeCountEnum.FCReadMongoCount);
        }};

        timePercentileMap = new HashMap<MonitorTimeEnum, TimePercentileEnum>() {{
                    put(MonitorTimeEnum.CookieManager,
                            TimePercentileEnum.CookieManagerPercentile);
                    put(MonitorTimeEnum.SplitTime,
                            TimePercentileEnum.SplitTimePercentile);
                    put(MonitorTimeEnum.RetargetingTime,
                            TimePercentileEnum.RetargetingTimePercentile);
                    put(MonitorTimeEnum.FrequencyTime,
                            TimePercentileEnum.FrequencyTimePercentile);
                    put(MonitorTimeEnum.AdsameCookieGet,
                            TimePercentileEnum.AdsameCookieGetPercentile);
                    put(MonitorTimeEnum.TotalTime,
                            TimePercentileEnum.TotalTimePercentile);
                    put(MonitorTimeEnum.FCApplyTime,
                            TimePercentileEnum.FCApplyPercentile);
                    put(MonitorTimeEnum.FCCommitTime,
                            TimePercentileEnum.FCCommitPercentile);
                    put(MonitorTimeEnum.FCReadMongoTime,
                            TimePercentileEnum.FCReadMongoPercentile);
        }};
    }

    public MonitorTime(MonitorTimeEnum enumTime,
            long time) {
        this.enumMonitorTime = enumTime;
        this.enumMonitorTimeCount = timeCountEnumMap.get(enumTime);
        this.enumPercentile = timePercentileMap.get(enumTime);
        this.time = time;
    }

    public static boolean isMonitorTimeField(String field) {
        for(MonitorTimeEnum enumTime : MonitorTimeEnum.values()) {
            if (field.equalsIgnoreCase(enumTime.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static MonitorTimeCountEnum getCorrespondTimeCount(
            MonitorTimeEnum timeEnum) {
        return timeCountEnumMap.get(timeEnum);
    }

    public static TimePercentileEnum getCorrespondTimePercentile(
            MonitorTimeEnum timeEnum) {
        return timePercentileMap.get(timeEnum);
    }
}
