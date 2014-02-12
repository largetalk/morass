/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.*;

public final class Alphabet {

    private int[] table;
    private int length;
    private byte[] revTable;

    public Alphabet(byte[] charSet) {
        if (charSet == null) {
            throw new IllegalArgumentException("init Alphabet failed");
        }
        table = new int[256];
        for(int i = 0; i < table.length; i++) {
            table[i] = -1;
        }

        for(byte b: charSet) {
            int index = b & 0xff; //byte to int
            table[index] = 0;
        }

        int seq = 0;
        for (int i = 0; i < table.length; i++) {
            if (table[i] != -1) {
                table[i] = seq++;
            }
        }
        this.length = seq;

        seq = 0;
        revTable = new byte[this.length];
        for (int i = 0; i < table.length; i++) {
            if (table[i] != -1) {
                revTable[seq++] = (byte) (i & 0xff); // int to byte
            }
        }
    }

    @Override
    public String toString() {
        return new String(revTable);
    }

    public int size() {
        return length;
    }

    public boolean isValid(byte c) {
        return table[c & 0xff] != -1;
    }

    public int getPos(byte c) {
        return table[c & 0xff];
    }

    public byte revertPos(int pos) {
        return revTable[pos % length];
    }

    public byte[] getHashChars(int hashCode) {
        byte[] hc = new byte[2];
        hc[0] = revertPos((int) (hashCode >>> 24) ^ ((hashCode >> 16) & 0xff));
        hc[1] = revertPos((int) ((hashCode >> 8) & 0xff) ^ (hashCode & 0xff));
        return hc;
    }

    public byte[] getRotorMap() {
        byte[] rotorMap = new byte[length];
        System.arraycopy(revTable, 0, rotorMap, 0, length);
        shuffleArray(rotorMap);
        return rotorMap;
    }

    public byte[] getReflectorMap() {
        byte[] reflectorMap = new byte[length];
        for (int i = 0; i < length; i++) {
            reflectorMap[i] = -1;
         }

        ArrayList<Byte> byteList = new ArrayList<Byte>();
        for (byte b : revTable) {
            byteList.add(b);
        }

        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            if (reflectorMap[i] == -1) {
                int index = rnd.nextInt(byteList.size());
                while (byteList.size() > 1 && index == 0) {
                    index = rnd.nextInt(byteList.size());
                }

                Byte c = byteList.get(index);
                reflectorMap[i] = c;
                reflectorMap[getPos(c)] = revertPos(i);
                byteList.remove(index);
                if (index != 0) {
                    byteList.remove(0);
                }
            }
        }
        return reflectorMap;
    }

    private static void shuffleArray(byte[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            byte a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
