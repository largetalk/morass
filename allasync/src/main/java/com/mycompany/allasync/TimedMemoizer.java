/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync;

import com.adsame.rtb.lib.util.TimeUtility;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class TimedMemoizer<A, V> {

    private final ConcurrentHashMap<A, Future<V>> cache =
            new ConcurrentHashMap<A, Future<V>>();
    private final HashMap<A, Long> occurTimeMap = new HashMap<A, Long>();
    
    private final Long expireTime;

    public TimedMemoizer(Long expireTime) {
        this.expireTime = expireTime;
    }
    
    public V compute(final Callable<V> c, final A arg) throws InterruptedException, Throwable {
        while (true) {
            Long now = TimeUtility.getTime();
            Long putTime;
            Future<V> f = cache.get(arg);
            synchronized (occurTimeMap) {
                putTime = occurTimeMap.get(arg);
            }
            if (f == null || now - putTime > expireTime) {
                FutureTask<V> ft = new FutureTask<V>(c);
                f = cache.putIfAbsent(arg, ft);
                if (f == null || now - putTime > expireTime) {
                    synchronized (occurTimeMap) {
                        occurTimeMap.put(arg, now);
                    }
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                cache.remove(arg, f);
            } catch (ExecutionException e) {
                throw new Throwable(e.getCause());
            }
        }
    }
    
    public static void main(String args[]) throws Throwable {
        TimedMemoizer<String, String> mem = new TimedMemoizer<String, String>(1000L);
        final String a = "abc";
        mem.compute(new Callable<String>() {

            @Override
            public String call() throws Exception {
                System.out.println(a);
                return a;
            }
        }, a);
    }
}
