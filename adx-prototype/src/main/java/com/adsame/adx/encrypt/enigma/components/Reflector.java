/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

public class Reflector extends Permutator {
    
    public static Reflector DEFAULT = new Reflector("uT6SUYHGgvN5jKX0kbDBErpOFteRh1a8IcnMQq2i9WlV4ZAJy3w7PdmxsLCzfo");

    private final String reflectorKey;

    public Reflector(String permutation) {
        this.reflectorKey = permutation;
    }

    public static Reflector create() {
        return new Reflector(Alphabet.getReflectorMap());
    }

    @Override
    public String toString() {
        return reflectorKey;
    }

    public char getCipherChar(char plainChar) {
        int pos = Alphabet.getPos(plainChar);
        return reflectorKey.charAt(pos);
    }

}
