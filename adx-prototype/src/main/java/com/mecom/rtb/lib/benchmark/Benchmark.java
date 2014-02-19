/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.benchmark;

import com.adsame.rtb.lib.benchmark.Metric.Counter;
import com.adsame.rtb.lib.benchmark.action.Action;
import com.adsame.rtb.lib.benchmark.action.ActionIterator;
import com.adsame.rtb.lib.benchmark.task.Task;
import com.adsame.rtb.lib.benchmark.task.impl.CyclicTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Benchmark {

    private class WorkerThread extends Thread {

        private Task task;
        private Counter counter;
        private AtomicReference<Boolean> keepsOn;
        private CountDownLatch exitLatch;

        public WorkerThread(Task task, Counter counter,
                AtomicReference<Boolean> keepsOn, CountDownLatch exitLatch) {
            this.task = task;
            this.counter = counter;
            this.keepsOn = keepsOn;
            this.exitLatch = exitLatch;
        }

        @Override
        public void run() {
            ActionIterator iterator = task.getActionIterator();
            while (keepsOn.get()) {
                counter.onStart();
                Action action = iterator.next();
                boolean returnValue = action.execute();
                if (returnValue) {
                    counter.onSucceed();
                } else {
                    counter.onFail();
                }
            }
            counter.dispose();
            exitLatch.countDown();
        }
    }

    public Report assess(Task tasks[], long duration) {
        AtomicReference<Boolean> keepsOn = new AtomicReference<Boolean>(true);
        CountDownLatch exitLatch = new CountDownLatch(tasks.length);
        Metric metric = new Metric();
        metric.turnOn();
        for (Task task : tasks) {
            Counter counter = metric.newCounter();
            CyclicTask cyclicTask = new CyclicTask(task);
            new WorkerThread(cyclicTask, counter, keepsOn, exitLatch).start();
        }
        try {
            Thread.sleep(duration);
            keepsOn.set(false);
            exitLatch.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        metric.turnOff();
        return metric.generateReport();
    }
}
