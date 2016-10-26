/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;


public class DoubleBufferedQueue<E> extends AbstractQueue<E> implements
        BlockingQueue<E>, Serializable{

    private static final long serialVersionUID = 1011398447523020L;
    public static final int DEFAULT_QUEUE_CAPACITY = 5000;
    private Logger log = Logger.getLogger(DoubleBufferedQueue.class);

    /** 读写锁分离 */
    private ReentrantLock readLock;
    private ReentrantLock writeLock;

    private Condition notFull;
    private Condition awake;

    private transient E[] writeArray;
    private transient E[] readArray;

    private volatile int writeCount;
    private volatile int readCount;

    private int writeArrayTP;
    private int writeArrayHP;

    private int readArrayTP;
    private int readArrayHP;

    private int capacity;

    @SuppressWarnings("unchecked")
    public DoubleBufferedQueue(int capacity){
        this.capacity = DEFAULT_QUEUE_CAPACITY;
        if (capacity > 0){
            this.capacity = capacity;
        }

        readArray = (E[])new Object[capacity];
        writeArray = (E[])new Object[capacity];

        readLock = new ReentrantLock();
        writeLock = new ReentrantLock();

        notFull = writeLock.newCondition();
        awake = writeLock.newCondition();
    }

    private void insert(E e){
        writeArray[writeArrayTP] = e;
        ++writeArrayTP;
        ++writeCount;
    }

    private E extract(){
        E e = readArray[readArrayHP];
        readArray[readArrayHP] = null;
        ++readArrayHP;
        --readCount;
        return e;
    }

    /**
     * 基础条件: 读队列 为空 && 发送队列不为空
     * 注意事项: 此函数在读的锁保持才会被调用 ,不然可能会造成死锁
     * @param timeout
     * @param isInfinite 是否一直等待直到其他线程唤醒它
     * @return
     */
    private long queueSwap(long timeout,boolean isInfinite)
            throws InterruptedException{
        writeLock.lock();
        try {
            if (writeCount <= 0){
                log.debug("Write Count:" + writeCount
                        + ", Write Queue is empty,do not switch");
                try {
                    if (isInfinite && timeout <= 0 ){
                        awake.await();
                        return -1;
                    }else {
                        return awake.awaitNanos(timeout);
                    }
                }catch (InterruptedException e){
                    awake.signal();
                    throw e;
                }
            }else {
                E[] tmpArray = readArray;
                readArray = writeArray;
                writeArray = tmpArray;

                readCount = writeCount;
                readArrayHP = 0;
                readArrayTP = writeArrayTP;

                writeCount = 0;
                writeArrayHP = readArrayHP;
                writeArrayTP = 0;

                notFull.signal();
                log.debug("Queue switch successfully!");
                return -1;
            }
        }finally {
            writeLock.unlock();
        }

    }
    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        int size = 0;
        readLock.lock();
        try {
            if (readCount > 0){
                size = readCount;
            }
        }finally {
            readLock.unlock();
        }
        return size;
    }

    @Override
    public void put(E e) throws InterruptedException {
        offer(e,500L,TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean offer(E e, long l, TimeUnit timeUnit) throws InterruptedException {
        if (e == null){
            throw new NullPointerException();
        }

        long nanoTime = timeUnit.toNanos(l);
        writeLock.lockInterruptibly();
        try {
            for (;;){
                if (writeCount < writeArray.length){
                    insert(e);
                    if (writeCount == 1){
                        awake.signal();
                    }
                    return true;
                }

                //Time out
                if (nanoTime <= 0){
                    log.debug("offer time out");
                    return false;
                }

                //Keep waiting
                try {
                    log.debug("queue is null , keeping waiting...");
                    nanoTime = notFull.awaitNanos(nanoTime);
                }catch (InterruptedException ie){
                    notFull.signal();
                    throw ie;
                }
            }
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        return poll(500L,TimeUnit.MILLISECONDS);
    }

    @Override
    public E poll(long l, TimeUnit timeUnit) throws InterruptedException {
        long nanoTime = timeUnit.toNanos(l);
        readLock.lockInterruptibly();

        try {
            for (;;){
                if (readCount > 0 ){
                    return extract();
                }
                if (nanoTime <=0){
                    log.debug("poll time out");
                    return null;
                }
                nanoTime = queueSwap(nanoTime,false);
            }
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        return this.capacity;
    }

    @Override
    public int drainTo(Collection<? super E> objects) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> objects, int i) {
        return 0;
    }

    @Override
    public boolean offer(E e) {
        boolean ret = false;
        try {
            ret = offer(e,500L,TimeUnit.MILLISECONDS);
        }catch (Exception e2){
            ret = false;
        }
        return ret;
    }

    @Override
    public E poll() {
        E ret = null;
        try {
            ret = poll(500L,TimeUnit.MILLISECONDS);
        }catch (Exception e){
            ret = null;
        }
        return ret;
    }

    @Override
    public E peek() {
        E e = null;
        readLock.lock();
        try {
            if (readCount >0 ){
                e = readArray[readArrayHP];
            }
        }finally {
            readLock.unlock();
        }
        return e;
    }

    public void clear(){
        writeLock.lock();
        try {
            readCount = 0;
            readArrayHP = 0;
            writeCount = 0;
            writeArrayTP = 0;
            log.debug("Queue clear successfully");
        }finally {
            writeLock.unlock();
        }
    }

    public static void main(String[] args) {
        try {
            DoubleBufferedQueue<String> q = new DoubleBufferedQueue<String>(1000);
            q.put("xxxx");
            q.put("xxxx");
            q.poll();
            System.out.println(q.size());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}