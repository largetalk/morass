/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;


public abstract class Permutator {

    protected Alphabet alphabet;
    protected byte[] permutation;

    public Permutator(Alphabet alphabet, byte[] permutation) {
        if (alphabet.size() != permutation.length) {
            throw new IllegalArgumentException("init permutator failed");
        }
        this.alphabet = alphabet;
        this.permutation = permutation;

    }

    public byte getCipherByte(byte inByte) {
        int pos = alphabet.getPos(inByte);
        return permutation[pos];
    }
}
