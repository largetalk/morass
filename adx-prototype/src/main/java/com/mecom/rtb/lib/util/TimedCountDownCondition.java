/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Timer;
import java.util.TimerTask;

public class TimedCountDownCondition {

    private boolean isEnabled;
    private Event event;

    private int counter;
    private long delay;
    private Timer timer;

    private Processor zeroCountProcessor;
    private Processor timeoutProcessor;

    boolean needsNotifying;

    private TimerTask timerTask;

    public TimedCountDownCondition(int counter, long delay, Timer timer) {
        initialize(counter, delay, timer, null, null, true);
    }

    public TimedCountDownCondition(int counter, long delay, Timer timer,
            Processor zeroCountProcessor, Processor timeoutProcessor,
            boolean needsNotifying) {
        initialize(counter, delay, timer, zeroCountProcessor, timeoutProcessor,
                needsNotifying);
    }

    public static enum Event {
        UNKNOWN,
        ZERO_COUNT,
        TIME_OUT,
    }

    // Implements this interface to do pre-processing before notifying
    // those threads who wait on this condition.
    public static interface Processor {

        public void process(Event event, int counter);
    }

    private class ConcreteTimerTask extends TimerTask {

        @Override
        public void run() {
            internalTrigger(Event.TIME_OUT, timeoutProcessor);
        }
    }

    public synchronized boolean isEnabled() {
        return isEnabled;
    }

    public synchronized void enable() {
        assertDisabled();
        isEnabled = true;
        timer.schedule(timerTask, delay);
    }

    public synchronized boolean isSatisfied() {
        return event != Event.UNKNOWN;
    }

    public synchronized Event getEvent() {
        return event;
    }

    public synchronized int getCounter() {
        return counter;
    }

    public synchronized void await() throws InterruptedException {
        assertEnabled();
        if (!isSatisfied()) {
            wait();
        }
    }

    public synchronized void countDown() {
        assertEnabled();
        if (!isSatisfied()) {
            counter = (counter >= 1) ? counter - 1 : counter;
            if (counter == 0) {
                internalTrigger(Event.ZERO_COUNT, zeroCountProcessor);
            }
        }
    }

    private void initialize(int counter, long delay, Timer timer,
            Processor zeroCountProcessor, Processor timeoutProcessor,
            boolean needsNotifying) {
        isEnabled = false;
        event = Event.UNKNOWN;

        if (counter <= 0) {
            throw new IllegalArgumentException("counter should > 0");
        }
        this.counter = counter;

        if (delay <= 0) {
            throw new IllegalArgumentException("delay should > 0");
        }
        this.delay = delay;

        if (timer == null) {
            throw new IllegalArgumentException("timer should not be null");
        }
        this.timer = timer;

        this.zeroCountProcessor = zeroCountProcessor;
        this.timeoutProcessor = timeoutProcessor;

        this.needsNotifying = needsNotifying;

        timerTask = new ConcreteTimerTask();
    }

    private synchronized void internalTrigger(Event actualEvent,
            Processor processor) {
        if (!isSatisfied()) {
            event = actualEvent;
            if (processor != null) {
                processor.process(event, counter);
            }
            if (needsNotifying) {
                notifyAll();
            }
        }
    }

    private synchronized void assertEnabled() {
        if (!isEnabled) {
            throw new IllegalStateException("condition has not been enabled");
        }
    }

    private synchronized void assertDisabled() {
        if (isEnabled) {
            throw new IllegalStateException("condition has been enabled");
        }
    }
}
