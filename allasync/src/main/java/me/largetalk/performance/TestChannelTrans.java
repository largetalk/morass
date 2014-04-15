/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class TestChannelTrans {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        RandomAccessFile fromFile = new RandomAccessFile("/tmp/from", "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("/tmp/to", "rw");
        FileChannel toChannel = toFile.getChannel();
        long position = 0;
        long count = fromChannel.size();
        toChannel.transferFrom(fromChannel, position, count);
        
        fromFile.close();
        toFile.close();
    }
}
