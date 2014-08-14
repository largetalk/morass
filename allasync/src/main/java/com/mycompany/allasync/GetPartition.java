/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync;

public class GetPartition {

    public static void main(String args[]) {
        String prefix = "data";
        String key = "4231";
        int numPartitions = 100;
        String realKey = prefix + key;
        int blockID = Math.abs(realKey.hashCode());
        System.out.println(blockID % numPartitions);
    }
}
