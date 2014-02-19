/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.ArrayList;

public final class ArraySearcher {

    private static final int LEFT = -1;
    private static final int CENTRAL = 0;
    private static final int RIGHT = 1;

    private static class Index {

        public int currentIndex;
        public int previouslyFoundIndex;

        public Index(int currentIndex, int previouslyFoundIndex) {
            this.currentIndex = currentIndex;
            this.previouslyFoundIndex = previouslyFoundIndex;
        }
    }

    // Use binary search to locate the occurrence of data in sorted list
    //
    // If no list element is == data, return -1
    //
    // e.g. list = {2, 4, 4, 4, 6, 8}
    //      data = 1                return -1
    //      data = 2                return 0
    //      data = 4                return 2
    //      data = 5                return -1
    //      data = 9                return -1
    public static <T extends Comparable> int locate(ArrayList<T> list, T data) {
        validateArguments(list, data);
        Index location = binarySearch(list, data, CENTRAL);
        return location.previouslyFoundIndex;
    }

    // Use binary search to locate the first occurrence of data in sorted list
    //
    // If no list element is == data, return -1
    //
    // e.g. list = {2, 4, 4, 4, 6, 8}
    //      data = 1                return -1
    //      data = 2                return 0
    //      data = 4                return 1
    //      data = 5                return -1
    //      data = 9                return -1
    public static <T extends Comparable> int locateFirst(ArrayList<T> list,
            T data) {
        validateArguments(list, data);
        Index location = binarySearch(list, data, LEFT);
        return location.previouslyFoundIndex;
    }

    // Use binary search to locate the last occurrence of data in sorted list
    //
    // If no list element is == data, return -1
    //
    // e.g. list = {2, 4, 4, 4, 6, 8}
    //      data = 1                return -1
    //      data = 2                return 0
    //      data = 4                return 3
    //      data = 5                return -1
    //      data = 9                return -1
    public static <T extends Comparable> int locateLast(ArrayList<T> list,
            T data) {
        validateArguments(list, data);
        Index location = binarySearch(list, data, RIGHT);
        return location.previouslyFoundIndex;
    }

    // Use binary search to locate the left bound of data in sorted list
    //
    // Here left bound is defined as the index of
    // the left-most list element which is >= data
    //
    // If no list element is >= data, return the length of list
    //
    // e.g. list = {2, 4, 4, 4, 6, 8}
    //      data = 1                return 0
    //      data = 2                return 0
    //      data = 4                return 1
    //      data = 5                return 4
    //      data = 9                return 6
    public static <T extends Comparable> int locateLeftBound(ArrayList<T> list,
            T data) {
        validateArguments(list, data);
        Index location = binarySearch(list, data, LEFT);
        int index = location.currentIndex;
        return (list.get(index).compareTo(data) >= 0) ? index : index + 1;
    }

    // Use binary search to locate the right bound of data in sorted list
    //
    // Here right bound is defined as the index of
    // the right-most list element which is <= data
    //
    // If no list element is <= data, return -1
    //
    // e.g. list = {2, 4, 4, 4, 6, 8}
    //      data = 1                return -1
    //      data = 2                return 0
    //      data = 4                return 3
    //      data = 5                return 3
    //      data = 9                return 5
    public static <T extends Comparable> int locateRightBound(ArrayList<T> list,
            T data) {
        validateArguments(list, data);
        Index location = binarySearch(list, data, RIGHT);
        int index = location.currentIndex;
        return (list.get(index).compareTo(data) <= 0) ? index : index - 1;
    }

    // This is a special implemetaion of binary search algorithm
    //
    // flag should be one of {LEFT, CENTRAL, RIGHT},
    // which indicate the direction preference during the searching
    private static <T extends Comparable> Index binarySearch(
            ArrayList<T> list, T data, int flag) {
        int currentIndex = -1;
        int previouslyFoundIndex = -1;

        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            currentIndex = (low + high) / 2;
            int comparison = list.get(currentIndex).compareTo(data);
            if (comparison < 0) {
                low = currentIndex + 1;
            } else if (comparison > 0) {
                high = currentIndex - 1;
            } else {
                previouslyFoundIndex = currentIndex;
                if (flag == LEFT) {
                    high = currentIndex - 1;
                } else if (flag == RIGHT) {
                    low = currentIndex + 1;
                } else {
                    break;
                }
            }
        }

        return new Index(currentIndex, previouslyFoundIndex);
    }

    private static <T extends Comparable> void validateArguments(
            ArrayList<T> list, T data) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("list must not be empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
    }
}
