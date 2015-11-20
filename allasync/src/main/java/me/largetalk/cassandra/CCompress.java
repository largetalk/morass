/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.cassandra;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;

public class CCompress {

    public static class User {

        private String name;
        public volatile int old;

        public User(String name, int old) {
            this.name = name;
            this.old = old;
        }

        public String getName() {
            return name;
        }

        public int getOld() {
            return old;
        }
    }

    public static void main(String args[]) throws InterruptedException {
        //ByteBuffer
        ByteBuffer bf1 = ByteBuffer.allocate(100);
        ByteBuffer bf2 = ByteBuffer.allocateDirect(100);
        System.out.println(bf1.isDirect());
        System.out.println(bf2.isDirect());
        System.out.println(bf1.order());

        //AtomicIntegerFieldUpdater
        AtomicIntegerFieldUpdater<User> a = AtomicIntegerFieldUpdater.newUpdater(User.class, "old");
        User conan = new User("conan", 10);
        System.out.println(a.getAndIncrement(conan));
        System.out.println(a.get(conan));
        a.compareAndSet(conan, 11, 12);
        System.out.println(a.get(conan));

        //LockSupport
        Thread thread = Thread.currentThread();
        LockSupport.unpark(thread);//释放许可 if comment this line, b will not be print
        LockSupport.park();// 获取许可
        System.out.println("b");

        metric();
    }

    public static void metric() throws InterruptedException {
        //metrics histogram
        MetricRegistry metrics = new MetricRegistry();
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();
        Histogram histogram = metrics.histogram(MetricRegistry.name(CCompress.class, "random"));
        reporter.start(3, TimeUnit.SECONDS);
        Random rand = new Random();
        long s_1 = System.currentTimeMillis();
        while (System.currentTimeMillis() - s_1 < 10000) {
            histogram.update(rand.nextInt());
            Thread.sleep(100);
        }
        reporter.stop();

        //metrics timer
        MetricRegistry timeMetrics = new MetricRegistry();
        ConsoleReporter timeReport = ConsoleReporter.forRegistry(timeMetrics).build();
        Timer requests = metrics.timer(MetricRegistry.name(CCompress.class, "timer"));
        timeReport.start(3, TimeUnit.SECONDS);
        long s_2 = System.currentTimeMillis();
        while (System.currentTimeMillis() - s_2 < 5000) {
            Timer.Context context = requests.time();
            try {
                //some operator
                Thread.sleep(rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                context.stop();
            }
        }
        timeReport.stop();
    }
}
