/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public final class CommonHelper {

    public static <T> boolean collectionSetOverlaps(
            Collection<T> collection, HashSet<T> set) {
        for (T value : collection) {
            if (set.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean arraySetOverlaps(T array[], HashSet<T> set) {
        for (T value : array) {
            if (set.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static <T> ArrayList<T> arrayToList(T array[]) {
        if (null == array) {
            return null;
        }
        return new ArrayList<T>(Arrays.asList(array));
    }
}
