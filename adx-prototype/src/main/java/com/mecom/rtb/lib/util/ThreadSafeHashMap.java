/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeHashMap<K, V> {

    private ConcurrentHashMap<K, V> hashMap =
            new ConcurrentHashMap<K, V>();
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void getReadLock() {
        rwLock.readLock().lock();
    }

    public void releaseReadLock() {
        rwLock.readLock().unlock();
    }

    public void getWriteLock() {
        rwLock.writeLock().lock();
    }

    public void releaseWriteLock() {
        rwLock.writeLock().unlock();
    }

    public int getSize() {
        return hashMap.size();
    }

    public V putValueIfAbsent(K key, V putValue) {
        V value;
        rwLock.readLock().lock();
        try {
            value = hashMap.get(key);
            if (value == null) {
                final V newValue = putValue;
                value = hashMap.putIfAbsent(key, newValue);
                if (value == null) {
                    value = newValue;
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return value;
    }

    public ConcurrentHashMap<K, V> getAndReplaceWithEmptyNew() {
        ConcurrentHashMap<K, V> oldHashMap;
        ConcurrentHashMap<K, V> newHashMap =
                new ConcurrentHashMap<K, V>();
        rwLock.writeLock().lock();
        try {
            oldHashMap = hashMap;
            hashMap = newHashMap;
        } finally {
            rwLock.writeLock().unlock();
        }
        return oldHashMap;
    }

    public void clear() {
        rwLock.readLock().lock();
        try {
            hashMap.clear();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
