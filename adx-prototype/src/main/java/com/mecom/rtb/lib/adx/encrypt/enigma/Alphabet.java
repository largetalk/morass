/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Alphabet {

    private final int indexTable[] = new int[256];
    private int length;
    private byte charSet[];

    public Alphabet(byte charSet[]) {
        if (charSet == null) {
            throw new IllegalArgumentException("init Alphabet failed");
        }
        for (int i = 0; i < indexTable.length; i++) {
            indexTable[i] = -1;
        }

        for (byte b : charSet) {
            int index = btoi(b); //byte to int
            indexTable[index] = 0;
        }

        int seq = 0;
        for (int i = 0; i < indexTable.length; i++) {
            if (indexTable[i] != -1) {
                indexTable[i] = seq++;
            }
        }
        this.length = seq;

        seq = 0;
        this.charSet = new byte[this.length];
        for (int i = 0; i < indexTable.length; i++) {
            if (indexTable[i] != -1) {
                this.charSet[seq++] = itob(i); // int to byte
            }
        }
    }

    public static byte itob(int i) {
        return (byte) (i & 0xff);
    }

    public static int btoi(byte b) {
        return b & 0xff;
    }

    public static Byte[] sbytes2bbytes(byte sbytes[]) {//byte[] to Byte[]
        Byte bbytes[] = new Byte[sbytes.length];
        int i = 0;
        for (byte b : sbytes) {
            bbytes[i++] = b;
        }
        return bbytes;
    }

    public static byte[] bbytes2sbytes(Byte bbytes[]) {
        byte sbytes[] = new byte[bbytes.length];
        int i = 0;
        for (Byte b : bbytes) {
            sbytes[i++] = b;
        }
        return sbytes;
    }

    @Override
    public String toString() {
        return new String(charSet);
    }

    public int size() {
        return length;
    }

    public byte[] getCharSet() {
        return charSet;
    }

    public boolean isValidByte(byte b) {
        return indexTable[btoi(b)] != -1;
    }

    public int getInnerFromOuter(byte b) {
        return indexTable[btoi(b)];
    }

    public byte getOuterFromInner(int innerCode) {
        return charSet[innerCode % length];
    }

    public byte[] getRandRotorPermutation() {
        Byte[] primitive = sbytes2bbytes(charSet);

        List<Byte> list = Arrays.asList(primitive);
        Collections.shuffle(list);

        byte[] rotorMap = new byte[charSet.length];
        for (int j = 0; j < rotorMap.length; j++) {
            rotorMap[j] = list.get(j);
        }
        return rotorMap;
    }

    public byte[] getRandReflectorPermutation() {
        Byte[] reflectorMap = new Byte[length];
        for (int i = 0; i < length; i++) {
            reflectorMap[i] = null;
        }

        ArrayList<Byte> byteList = new ArrayList<Byte>();
        for (byte b : charSet) {
            byteList.add(b);
        }

        Random rnd = new Random();
        for (int i = 0; i < length && byteList.size() > 1; i++) {
            if (reflectorMap[i] == null) {
                int index = rnd.nextInt(byteList.size() - 1) + 1;

                Byte c = byteList.get(index);
                reflectorMap[i] = c;
                reflectorMap[getInnerFromOuter(c)] = getOuterFromInner(i);
                byteList.remove(index);
                byteList.remove(0);
            }
        }
        if (!byteList.isEmpty()) {//charSet length is odd
            Byte c = byteList.get(0);
            reflectorMap[getInnerFromOuter(c)] = byteList.get(0);
        }

        return bbytes2sbytes(reflectorMap);
    }
}
