/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

public class Reflector extends Permutator {
    
    //public static Reflector DEFAULT = new Reflector("uT6SUYHGgvN5jKX0kbDBErpOFteRh1a8IcnMQq2i9WlV4ZAJy3w7PdmxsLCzfo");


    public Reflector(Alphabet alphabet, byte[] permutation) {
        super(alphabet, permutation);
    }

    @Override
    public String toString() {
        return permutation.toString();
    }
}
