/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

public interface ObjectFactory<T, R> {

    public T createObject(R parameter);
}
