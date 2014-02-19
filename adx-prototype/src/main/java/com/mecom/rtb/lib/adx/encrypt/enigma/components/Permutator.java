/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma.components;

public abstract class Permutator {

    protected int requiredLength;
    protected int permutation[];

    public Permutator(int requiredLength, int permutation[]) {
        if (!isValid(requiredLength, permutation)) {
            throw new IllegalArgumentException("init permutator failed");
        }
        this.requiredLength = requiredLength;
        this.permutation = permutation;

    }

    @Override
    public String toString() {
        return String.valueOf(permutation);
    }

    public int getCipherCode(int innerCode) {
        return permutation[innerCode];
    }

    private static boolean isValid(int length, int permutation[]) {
        if (permutation.length != length) {
            return false;
        }

        boolean check[] = new boolean[length];
        for (int index : permutation) {
            if (index >= length || index < 0) {
                return false;
            }
            if (check[index]) { //duplicate
                return false;
            }
            check[index] = true;
        }
        return true;
    }
}
