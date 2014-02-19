/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

public class ThreadLocalWrapper<T, R> {

    private final ThreadLocal<T> threadLocalObject = new ThreadLocal<T>();
    private final ObjectFactory<T, R> objectFactory;
    private final R defaultParameter;

    public ThreadLocalWrapper(ObjectFactory<T, R> objectFactory) {
        this(objectFactory, null);
    }

    public ThreadLocalWrapper(ObjectFactory<T, R> objectFactory,
            R defaultParameter) {
        this.objectFactory = objectFactory;
        this.defaultParameter = defaultParameter;
    }

    public T getObject() {
        T object = threadLocalObject.get();
        if (object == null) {
            object = resetObject(defaultParameter);
        }
        return object;
    }

    public T resetObject(R parameter) {
        T object = objectFactory.createObject(parameter);
        threadLocalObject.set(object);
        return object;
    }

    public T resetObject() {
        T object = objectFactory.createObject(defaultParameter);
        threadLocalObject.set(object);
        return object;
    }
}
