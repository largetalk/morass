/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

public class TestTimer {

    public static void main(String args[]) {
        for (int i = 0; i < 100; i++) {
            System.out.println(SystemTimer.currentTimeMillis() + " - " +
                    System.currentTimeMillis() + " => " +
                    (SystemTimer.currentTimeMillis() - System.currentTimeMillis()));
            try {
                Thread.sleep(65);
            } catch (InterruptedException ex) {}
        }
        
        long last = 0;
        long start = System.currentTimeMillis();
        System.out.println(start);
        for (int i = 0; i < 10000000; i++) {
            last = System.currentTimeMillis();
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(last);
        System.out.println("=============");

        start = System.currentTimeMillis();
        System.out.println(start);
        for (int i = 0; i < 10000000; i++) {
            last = SystemTimer.currentTimeMillis();
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(last);
        
        System.exit(0);
    }
}
