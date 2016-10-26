/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.counter;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.FloatAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.ConfigurationUtility;
import com.adsame.rtb.lib.redis.util.RedisCluster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisCounter implements Counter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RedisCounter.class);

    private Section section;
    private RedisCluster redisCluster;
    private RedisBloomFilter redisBloomFilter;
    private String bloomFilterPrefix;
    private String countPrefix;

    @MetaAnnotation(
            prefix = "redis-counter",
            comment = "redis counter configuration")
    public static final class Meta {

        @FloatAnnotation(
                comment = "error rate")
        public static final String ERROR_RATE = "errorRate";

        @IntegerAnnotation(
                comment = "max key")
        public static final String MAX_KEY = "maxKey";

        @StringAnnotation(
                comment = "bloom filter prefix")
        public static final String BLOOM_FILTER_PREFIX = "bloomFilterPrefix";

        @StringAnnotation(
                comment = "count prefix")
        public static final String COUNT_PREFIX = "countPrefix";
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);
        redisCluster = ConfigurationUtility.createInstance(configuration,
                RedisCluster.class, "redis cluster");
        Float errorRate = (Float) section.get(Meta.ERROR_RATE);
        Integer maxKey = (Integer) section.get(Meta.MAX_KEY);
        redisBloomFilter = new RedisBloomFilter(redisCluster.getJedisCluster(),
                errorRate, maxKey);
        bloomFilterPrefix = (String) section.get(Meta.BLOOM_FILTER_PREFIX);
        countPrefix = (String) section.get(Meta.COUNT_PREFIX);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
        redisCluster.saveConfigurationRecursively(outputStream, level + 1);
    }

    @Override
    public void add(String pointID, List<String> cookies) {
        String filterKey = getFilterKey(pointID);
        String countKey = getCountKey(pointID);
        for (String cookie : cookies) {
            int hc = cookie.hashCode();
            if (!redisBloomFilter.contain(filterKey, hc)) {
                redisBloomFilter.add(filterKey, hc);
                redisCluster.getJedisCluster().incr(countKey);
            }
        }
    }

    @Override
    public long getCount(String pointID) {
        String countKey = getCountKey(pointID);
        String countStr = redisCluster.getJedisCluster().get(countKey);
        if (countStr == null) {
            return 0;
        }
        try {
            return Long.parseLong(countStr);
        } catch (NumberFormatException ex) {
            LOGGER.error("get count {}  return {}", pointID, countStr);
            return 0;
        }
    }

    @Override
    public boolean clear(String pointID) {
        String filterKey = getFilterKey(pointID);
        String countKey = getCountKey(pointID);
        redisBloomFilter.clear(filterKey);
        redisCluster.getJedisCluster().del(countKey);
        return true;
    }

    private String getFilterKey(String key) {
        return bloomFilterPrefix + "::" + key;
    }

    private String getCountKey(String key) {
        return countPrefix + "::" + key;
    }
}
