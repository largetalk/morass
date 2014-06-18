/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.qdigest;

import com.clearspring.analytics.stream.quantile.QDigest;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class Test {
    
    public static void main(String args[]) {
        System.out.println(Long.highestOneBit(6));
        System.out.println(Long.highestOneBit(255));
        System.out.println(Long.highestOneBit(4));
        System.out.println(Long.highestOneBit(7));
        System.out.println(Long.highestOneBit(8));
        System.out.println("===========================");
        
        QDigest digest = new QDigest(100);
        for (int i = 1; i <= 10000; i++) {
            digest.offer(i);
        }
        System.out.println(digest.computeActualSize());
        System.out.println(digest.getQuantile(0.1));
        System.out.println(digest.getQuantile(0.25));
        System.out.println(digest.getQuantile(0.5));
        System.out.println(digest.getQuantile(0.75));
        System.out.println(digest.getQuantile(0.8));
        System.out.println("===========================");
        
        Long2LongOpenHashMap node2count = new Long2LongOpenHashMap();
        node2count.put(1, 100);
        node2count.put(2, 100);
        node2count.put(3, 100);
        
        node2count.addTo(1, 100);
        node2count.addTo(4, 100);
        
        for (Long k: node2count.keySet()) {
            System.out.print(k + " -- ");
            System.out.println(node2count.get(k));
        }
    }
}
