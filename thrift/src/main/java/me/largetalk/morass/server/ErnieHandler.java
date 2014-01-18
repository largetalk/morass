package me.largetalk.morass.server;

/**
 * Created by largetalk on 1/18/14.
 */
import me.largetalk.morass.thrift.Ernie;
import me.largetalk.morass.thrift.Impression;
import me.largetalk.morass.thrift.LuckAd;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicLongMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class ErnieHandler implements Ernie.Iface {
    private static Logger logger = LoggerFactory.getLogger(ErnieHandler.class);
    //private Map<String, Integer> counter;
    private ConcurrentMap<String, AtomicLong> counter;

    private long startTime, endTime;

    public ErnieHandler() {
        counter =  new ConcurrentHashMap<String, AtomicLong>();
    }

    public LuckAd bet(Impression impl) {
        try {
            startTime = System.nanoTime();

            logger.info(" Ernie handler " + impl.toString());
            counter.putIfAbsent(impl.asid, new AtomicLong(0));
            counter.get(impl.asid).incrementAndGet();

            return new LuckAd("fake bid", counter.toString());
        } finally {
            endTime = System.nanoTime();
            logger.info("bet executed time {} 微秒", (endTime - startTime)/1000);
        }
    }
}
