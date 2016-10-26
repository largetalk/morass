/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class TestC {

    private static final String ISO = "ISO-8859-1";
    private static final String LATIN1 = "Latin1";

    public static void main(String args[]) throws UnsupportedEncodingException {

        Random rand = new Random();
        byte d[] = new byte[10000];
        rand.nextBytes(d);
        for (int i = 0; i < d.length; i++) {
            //    System.out.println(d[i]);
        }
        System.out.println("#####################");
        String e = new String(d, ISO);
        System.out.println(e);
        System.out.println(e.length());
        System.out.println("#####################");
        byte[] f = e.getBytes("ISO-8859-1");
        for (int i = 0; i < f.length; i++) {
            //    System.out.println(f[i]);
        }
        System.out.println(Arrays.equals(d, f));
        System.out.println("######################################");

        byte g[] = new byte[10];
        rand.nextBytes(g);
        for (int i = 0; i < g.length; i++) {
            System.out.println(g[i]);
        }
        System.out.println("#####################");
        String h = Arrays.toString(g);
        System.out.println(h);
        System.out.println("#####################");
        String[] byteValues = h.substring(1, h.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
            System.out.println(bytes[i]);
        }
        System.out.println("######################################");
        
        long al = 3482934298l;
        byte[] bal = longToBytes(al);
        String sal = new String(bal, ISO);
        long nal = Long.parseLong(sal);
        System.out.println(al);
        System.out.println(nal);
        System.out.println("######################################");
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }
}
