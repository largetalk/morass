/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.benchmark;

import com.adsame.rtb.lib.stat.Statistics;

public class Report {

    public int numCounters;

    public long startTime;
    public long endTime;
    public long duration;

    public int numActions;
    public int numUnknownActions;
    public int numSucceededActions;
    public int numFailedActions;

    public double queryPerSecond;

    public long maxLatency;
    public long minLatency;
    public double meanLatency;
    public double medianLatency;

    public Statistics latencyStatistics;

    public Report() {
        numCounters = 0;

        startTime = -1;
        endTime = -1;
        duration = 0;

        numActions = 0;
        numUnknownActions = 0;
        numSucceededActions = 0;
        numFailedActions = 0;

        queryPerSecond = 0.0;

        maxLatency = 0;
        minLatency = 0;
        meanLatency = 0.0;
        medianLatency = 0.0;

        latencyStatistics = null;
    }

    public Report(Report report) {
        numCounters = report.numCounters;

        startTime = report.startTime;
        endTime = report.endTime;
        duration = report.duration;

        numActions = report.numActions;
        numUnknownActions = report.numUnknownActions;
        numSucceededActions = report.numSucceededActions;
        numFailedActions = report.numFailedActions;

        queryPerSecond = report.queryPerSecond;

        maxLatency = report.maxLatency;
        minLatency = report.minLatency;
        meanLatency = report.meanLatency;
        medianLatency = report.medianLatency;

        latencyStatistics = report.latencyStatistics;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Counters : ");
        builder.append(numCounters).append("\n");
        builder.append("Start Time : ");
        builder.append(startTime).append("\n");
        builder.append("End Time : ");
        builder.append(endTime).append("\n");
        builder.append("Duration : ");
        builder.append(duration).append("\n");
        builder.append("Actions : ");
        builder.append(numActions).append("\n");
        builder.append("Unknown Actions : ");
        builder.append(numUnknownActions).append("\n");
        builder.append("Succeeded Actions : ");
        builder.append(numSucceededActions).append("\n");
        builder.append("Failed Actions : ");
        builder.append(numFailedActions).append("\n");
        builder.append("Query Per Second : ");
        builder.append(queryPerSecond).append("\n");
        builder.append("Max Latency : ");
        builder.append(maxLatency).append("\n");
        builder.append("Min Latency : ");
        builder.append(minLatency).append("\n");
        builder.append("Mean Latency : ");
        builder.append(meanLatency).append("\n");
        builder.append("Median Latency : ");
        builder.append(medianLatency);
        return builder.toString();
    }
}
