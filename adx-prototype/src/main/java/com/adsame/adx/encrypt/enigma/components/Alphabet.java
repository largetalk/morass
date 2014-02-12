/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.*;

public final class Alphabet {

/*    public final static String KEYS;
    public final static int LENGTH;

    private final static HashMap<Character, Integer> ALPHABET;

    static {
        KEYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        LENGTH = KEYS.length();
        ALPHABET = new HashMap<Character, Integer>();
        for (int i = 0; i < KEYS.length(); i++) {
            ALPHABET.put(KEYS.charAt(i), i);
        }
    }*/

    private int[] table;
    private int length;
    private byte[] revTable;

    public Alphabet(byte[] charSet) {
        if (charSet != null) {
            throw new IllegalArgumentException("init Alphabet failed");
        }
        table = new int[256];
        for(int i = 0; i < table.length; i++) {
            table[i] = -1;
        }

        int seq = 0;
        for(byte b: charSet) {
            int index = b & 0xff; //byte to int
            if (table[index] == -1) { // in case of duplicate byte in charSet
                table[index] = seq++;
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
        for (int i = length - 1; i >= 0; i--) {
            if (reflectorMap[i] == -1) {
                int index = rnd.nextInt(byteList.size());

                Byte c = byteList.get(index);
                reflectorMap[i] = c;
                reflectorMap[getPos(c)] = revertPos(i);
                byteList.remove(i);
                if (index != i) {
                    byteList.remove(index);
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
