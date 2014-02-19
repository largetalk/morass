/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

public class FSTimer extends Timer {

    private long periodMilliSeconds;

    public FSTimer(long periodMilliSeconds) {
        this.periodMilliSeconds = periodMilliSeconds;
    }

    public class FSTask extends TimerTask {

        @Override
        public final void run() {
            runFSTask(floorNowToPeriod(
                    Calendar.getInstance().getTimeInMillis()));
        }

        public void runFSTask(long now) {
        }
    }

    public void schedule(TimerTask fsTimerTask) {
        long now = Calendar.getInstance().getTimeInMillis();
        scheduleAtFixedRate(fsTimerTask,
                getIntervalFromNowToCeiling(now), periodMilliSeconds);
    }

    private long floorNowToPeriod(long now) {
        long interval = (now - TimeUtility.getDayBasedFloor(now)) % periodMilliSeconds;
        return now - interval;
    }

    private long getIntervalFromNowToCeiling(long now) {
        long interval = periodMilliSeconds - (now - floorNowToPeriod(now));
        return interval;
    }
}
