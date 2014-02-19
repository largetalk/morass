/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerPool {

    private final Timer timerArray[];
    private final AtomicInteger index;

    public TimerPool(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size of timer pool should > 0");
        }

        timerArray = new Timer[size];
        for (int i = 0; i < size; i++) {
            timerArray[i] = new Timer(true);
        }
        index = new AtomicInteger(0);
    }

    public Timer getTimer() {
        if (timerArray.length == 1) {
            return timerArray[0];
        }

        int oldIndex;
        int newIndex;
        do {
            oldIndex = index.get();
            newIndex = (oldIndex == timerArray.length - 1) ? 0 : oldIndex + 1;
        } while (!index.compareAndSet(oldIndex, newIndex));
        return timerArray[oldIndex];
    }
}
