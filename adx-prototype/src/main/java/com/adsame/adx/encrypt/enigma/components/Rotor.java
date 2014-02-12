/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.HashMap;

public class Rotor extends Permutator {
    
    //public static final String I = "JqBDko4fZyzt1Yr6GRMFxOUIalXPnELNcs823hAvKH905gdmp7CTVbuQeiWSwj";
    //public static final String II = "LbzPDGJU3nf81OV5BHqCrQcAlpohFm7aSjdMRKEvXNg6s0T4ZWty2w9YiIeuxk";
    //public static final String III = "bWhqJ1AeX8kzfMCRN0dUZL4wnYpSmgGExjvHyDFVKT35BQ6uP29rclI7staioO";
    //public static final String IV = "fJwk3X0IAZVacziSbdj6xtCvgYD8lr7s5PL4eFuB2UTn1GpMhmRyqQHEON9WoK";

    private int position = 0;
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
        return permutation.toString() + " " + String.valueOf(position);
    }

    public byte execute(byte input) {
        int pos = alphabet.getPos(input) + position;
        pos = pos % permutation.length;
        return permutation[pos];
    }

    public byte revert(byte output) {
        int pos = map.get(output);
        pos = pos - position;
        if (pos < 0) {
            pos += permutation.length;
        }
        return alphabet.revertPos(pos);
    }

    public void setPosition(int position) {
        this.position = Math.abs(position) % permutation.length;

    }

    public void setPosition(byte ch) {
        setPosition(alphabet.getPos(ch));
    }

    public void rotate() {
        position = (position + 1) % permutation.length;
    }
}
