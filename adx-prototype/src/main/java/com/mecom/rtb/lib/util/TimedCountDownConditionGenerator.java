/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.rtb.lib.util.TimedCountDownCondition.Processor;
import java.util.Timer;

public class TimedCountDownConditionGenerator {

    private TimerPool timerPool;

    public TimedCountDownConditionGenerator(int numTimers) {
        timerPool = new TimerPool(numTimers);
    }

    public TimedCountDownCondition generate(int counter, long delay) {
        Timer timer = timerPool.getTimer();
        TimedCountDownCondition condition = new TimedCountDownCondition(
                counter, delay, timer);
        return condition;
    }

    public TimedCountDownCondition generate(int counter, long delay,
            Processor zeroCountProcessor, Processor timeoutProcessor,
            boolean needsNotifying) {
        Timer timer = timerPool.getTimer();
        TimedCountDownCondition condition = new TimedCountDownCondition(
                counter, delay, timer, zeroCountProcessor, timeoutProcessor,
                needsNotifying);
        return condition;
    }
}
