/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

public class TestTimer {

    public static void main(String args[]) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            System.currentTimeMillis();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }
}
