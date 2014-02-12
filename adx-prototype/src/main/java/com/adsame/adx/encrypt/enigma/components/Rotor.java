/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.HashMap;

public class Rotor extends Permutator {

    private HashMap<Byte, Integer> map;
    private byte[] revPermutation; // for replace map

    public Rotor(Alphabet alphabet, byte[] permutation) {
        super(alphabet, permutation);

        revPermutation = new byte[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            int pos = alphabet.getPos(permutation[i]);
            revPermutation[pos] = alphabet.revertPos(i);
        }

        this.map = new HashMap<Byte, Integer>();
        for (int i = 0; i < permutation.length; i++) {
            map.put(permutation[i], i);
        }
    }
    
    @Override
    public String toString() {
        return permutation.toString();
    }

    public byte execute(byte input, int position) {
        int pos = alphabet.getPos(input) + position;
        pos = pos % permutation.length;
        return permutation[pos];
    }

    public byte revert(byte output, int position) {
        int pos = map.get(output);
        pos = pos - position;
        if (pos < 0) {
            pos += permutation.length;
        }
        return alphabet.revertPos(pos);
    }
}
