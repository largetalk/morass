/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.counter;

import redis.clients.jedis.JedisCluster;

public class RedisBloomFilter {

    private final JedisCluster jc;
    private final int maxKey;
    private final float errorRate;
    private final int bitSize;
    private final int hashFunctionCount;

    public RedisBloomFilter(JedisCluster jc, float errorRate, int maxKey) {
        this.jc = jc;
        this.errorRate = errorRate;
        this.maxKey = maxKey;
        bitSize = calcOptimalM(maxKey, errorRate);
        hashFunctionCount = calcOptimalK(bitSize, maxKey);
    }

    public void add(String key, long bizId) {
        int[] offset = HashUtils.murmurHashOffset(bizId, hashFunctionCount, bitSize);
        for (int i : offset) {
            jc.setbit(key, i, true);
        }
    }

    public boolean contain(String key, long bizId) {
        int[] offset = HashUtils.murmurHashOffset(bizId, hashFunctionCount, bitSize);

        for (int i : offset) {
            if (!jc.getbit(key, i)) {
                return false;
            }
        }
        return true;
    }

    public long count(String key) {
        return jc.bitcount(key);
    }

    public void clear(String key) {
        jc.del(key);
    }

    // please see: https://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives
    public int calcOptimalM(int maxKey, float errorRate) {
        return (int) Math.ceil(maxKey
                * (Math.log(errorRate) / Math.log(0.6185)));
    }

    public int calcOptimalK(int bitSize, int maxKey) {
        return (int) Math.ceil(Math.log(2) * (bitSize / maxKey));
    }
}
