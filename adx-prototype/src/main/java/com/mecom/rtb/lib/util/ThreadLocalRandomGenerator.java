/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Random;

public class ThreadLocalRandomGenerator implements RandomGenerator {

    private ThreadLocal<Random> threadLocalRandom;

    public ThreadLocalRandomGenerator() {
        threadLocalRandom = new ThreadLocal<Random>() {

            @Override
            protected Random initialValue() {
                return new Random();
            }
        };
    }

    @Override
    public double nextDouble() {
        return threadLocalRandom.get().nextDouble();
    }
}
