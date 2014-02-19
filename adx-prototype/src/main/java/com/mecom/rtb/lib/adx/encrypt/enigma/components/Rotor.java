/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma.components;

public class Rotor extends Permutator {

    private final int indexTable[];

    public Rotor(int requiredLength, int permutation[]) {
        super(requiredLength, permutation);

        indexTable = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            indexTable[permutation[i]] = i;
        }
    }

    public int execute(int innerCode, int offset) {
        int pos = innerCode + offset;
        pos = pos % permutation.length;
        return permutation[pos];
    }

    public int revert(int innerCode, int offset) {
        int pos = indexTable[innerCode];
        pos = pos - offset % permutation.length;
        if (pos < 0) {
            pos += permutation.length;
        }
        return pos;
    }
}
