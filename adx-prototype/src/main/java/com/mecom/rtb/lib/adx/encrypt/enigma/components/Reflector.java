/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma.components;

public class Reflector extends Permutator {

    public Reflector(int requiredLength, int permutation[]) {
        super(requiredLength, permutation);

        for (int i = 0; i < permutation.length; i++) {
            int elem = getCipherCode(permutation[i]);
            int reflectCode = getCipherCode(elem);
            if (permutation[i] != reflectCode) {
                throw new IllegalArgumentException("illegal reflector permutator");
            }
        }
    }
}
