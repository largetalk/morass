/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.performance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class TestSelect {

    private static byte end = '\n';

    public static void main(String args[]) throws IOException, InterruptedException {
        ServerSocketChannel channel = null;

        try {
            Selector selector = Selector.open();
            channel = ServerSocketChannel.open();
            ServerSocket serverSocket = channel.socket();
            serverSocket.setReuseAddress(true);
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
                    if (!key.isValid()) {
                        key.channel().close();
                        continue;
                    }
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(key.selector(), SelectionKey.OP_READ);
                        System.out.println("a connection was accepted by a ServerSocketChannel.");
                        // a connection was accepted by a ServerSocketChannel.
                    } else if (key.isConnectable()) {
                        System.out.println("a connection was established with a remote server.");
                        // a connection was established with a remote server.
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        int bytesRead = socketChannel.read(buf);
                        while (bytesRead != -1) {
                            System.out.println("req >>> " + bytesRead + end);
                            byte last = buf.get(buf.position() - 1);
                            if (last == end) {
                                break;
                            }
                            bytesRead = socketChannel.read(buf);
                            Thread.sleep(500);
                        }
                        buf.flip();
                        Charset charset = Charset.defaultCharset();
                        CharBuffer charBuffer = charset.decode(buf);
                        System.out.println("a channel is ready for reading" + charBuffer.toString());
                        key.interestOps(SelectionKey.OP_WRITE);
                        // a channel is ready for reading
                    } else if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        Charset charset = Charset.defaultCharset();
                        String data = "S : " + System.currentTimeMillis();
                        ByteBuffer byteBuffer = charset.encode(data);
                        int limit = byteBuffer.limit();
                        byteBuffer.clear();
                        byteBuffer.position(limit);
                        byteBuffer.put(end);
                        byteBuffer.flip();
                        while (byteBuffer.hasRemaining()) {
                            socketChannel.write(byteBuffer);
                        }
                        //key.cancel();
                        key.interestOps(SelectionKey.OP_READ);
                        System.out.println("a channel is ready for writing");
                        // a channel is ready for writing
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            channel.close();
        }
    }
}
