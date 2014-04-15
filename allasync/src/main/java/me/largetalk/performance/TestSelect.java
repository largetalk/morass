/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TestSelect {

    public static void main(String args[]) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel channel = ServerSocketChannel.open();
        ServerSocket serverSocket = channel.socket();
        serverSocket.bind(new InetSocketAddress(8088));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }
            Set selectedKeys = selector.selectedKeys();
            Iterator keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key;
                key = (SelectionKey) keyIterator.next();
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("a connection was accepted by a ServerSocketChannel.");
                    // a connection was accepted by a ServerSocketChannel.
                } else if (key.isConnectable()) {
                    System.out.println("a connection was established with a remote server.");
                    // a connection was established with a remote server.
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(100);
                    int bytesRead = socketChannel.read(buf);
                    while (bytesRead != -1) {
                        System.out.println("req >>> " + bytesRead);
                        buf.flip();
                        while (buf.hasRemaining()) {
                            System.out.print((char) buf.get());
                        }
                        buf.clear();
                        bytesRead = socketChannel.read(buf);
                    }
                    System.out.println("a channel is ready for reading");
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                    // a channel is ready for reading
                } else if (key.isWritable()) {
                    System.out.println("a channel is ready for writing");
                    // a channel is ready for writing
                }
                keyIterator.remove();
            }
        }
    }
}
