/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Random;

public class GlobalRandomGenerator implements RandomGenerator {

    private static final long SEED = 0;

    private Random random;

    public GlobalRandomGenerator() {
        random = new Random(SEED);
    }

    @Override
    public synchronized double nextDouble() {
        return random.nextDouble();
    }
}
