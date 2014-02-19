/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.benchmark;

import com.adsame.rtb.lib.stat.Statistics;
import java.util.LinkedList;

class Metric {

    private boolean isEverOn;
    private boolean isEverOff;

    private int numAliveCounters;

    private Statistics statistics;

    private Report report;

    public Metric() {
        isEverOn = false;
        isEverOff = false;

        numAliveCounters = 0;

        statistics = new Statistics();

        report = new Report();
    }

    public class Counter {

        private boolean isDisposed;

        private int numActions;
        private int numUnknownActions;
        private int numSucceededActions;
        private int numFailedActions;

        private long startTime;
        private long endTime;
        private LinkedList<Long> latencyList;

        private Counter() {
            isDisposed = false;

            numActions = 0;
            numUnknownActions = 0;
            numSucceededActions = 0;
            numFailedActions = 0;

            latencyList = new LinkedList<Long>();
        }

        public synchronized void onStart() {
            if (!isDisposed) {
                numActions += 1;
                numUnknownActions += 1;

                startTime = System.currentTimeMillis();
            }
        }

        public synchronized void onSucceed() {
            if (!isDisposed) {
                numSucceededActions += 1;
                numUnknownActions -= 1;

                endTime = System.currentTimeMillis();
                latencyList.add(endTime - startTime);
            }
        }

        public synchronized void onFail() {
            if (!isDisposed) {
                numFailedActions += 1;
                numUnknownActions -= 1;

                endTime = System.currentTimeMillis();
                latencyList.add(endTime - startTime);
            }
        }

        public synchronized void dispose() {
            if (!isDisposed) {
                Metric.this.merge(this);
                isDisposed = true;
            }
        }
    }

    public synchronized void turnOn() {
        if (isEverOn) {
            return;
        }
        isEverOn = true;
        report.startTime = System.currentTimeMillis();
    }

    public synchronized void turnOff() {
        if (!isEverOn || isEverOff) {
            return;
        }
        if (numAliveCounters != 0) {
            return;
        }
        isEverOff = true;

        report.endTime = System.currentTimeMillis();
        report.duration = report.endTime - report.startTime;

        report.queryPerSecond = 1000.0 * report.numSucceededActions
                / report.duration;

        report.maxLatency = (long) statistics.getMax();
        report.minLatency = (long) statistics.getMin();
        report.meanLatency = statistics.getMean();
        report.medianLatency = statistics.getMedian();

        report.latencyStatistics = statistics;
    }

    public synchronized Counter newCounter() {
        report.numCounters += 1;
        numAliveCounters += 1;
        return new Counter();
    }

    public synchronized Report generateReport() {
        return isEverOff ? new Report(report) : null;
    }

    private synchronized void merge(Counter counter) {
        report.numActions += counter.numActions;
        report.numUnknownActions += counter.numUnknownActions;
        report.numSucceededActions += counter.numSucceededActions;
        report.numFailedActions += counter.numFailedActions;

        numAliveCounters -= 1;

        statistics.add(counter.latencyList);
    }
}
