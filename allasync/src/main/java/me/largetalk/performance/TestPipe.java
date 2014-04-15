/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class TestPipe {

    public static void main(String args[]) throws IOException {
        Pipe pipe = Pipe.open();
        Pipe.SinkChannel sinkChannel = pipe.sink();

        String newData = "New String to write to file..." + System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            sinkChannel.write(buf);
        }

        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer bufo = ByteBuffer.allocate(48);

        int bytesRead = sourceChannel.read(bufo);

        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead);
            bufo.flip();
            while (bufo.hasRemaining()) {
                System.out.println((char) bufo.get());
            }
            bufo.clear();
            bytesRead = sourceChannel.read(bufo);
        }

    }
}
