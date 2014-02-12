/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

public class Reflector extends Permutator {


    public Reflector(Alphabet alphabet, byte[] permutation) {
        super(alphabet, permutation);
    }

    @Override
    public String toString() {
        return new String(permutation);
    }

    public Alphabet getAlphabet() {
        return this.alphabet;
    }
}
