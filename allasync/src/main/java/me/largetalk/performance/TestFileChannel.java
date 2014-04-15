/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestFileChannel {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        RandomAccessFile aFile = new RandomAccessFile("/tmp/abc", "rw");
        FileChannel fChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(16);
        int bytesRead = fChannel.read(buf);
        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead);
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.println((char) buf.get());
            }
            buf.clear();
            bytesRead = fChannel.read(buf);
        }
        aFile.close();
    }
}
