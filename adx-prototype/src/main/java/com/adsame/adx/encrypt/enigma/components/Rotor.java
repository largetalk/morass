/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.HashMap;

public class Rotor extends Permutator {
    
    public static final String I = "JqBDko4fZyzt1Yr6GRMFxOUIalXPnELNcs823hAvKH905gdmp7CTVbuQeiWSwj";
    public static final String II = "LbzPDGJU3nf81OV5BHqCrQcAlpohFm7aSjdMRKEvXNg6s0T4ZWty2w9YiIeuxk";
    public static final String III = "bWhqJ1AeX8kzfMCRN0dUZL4wnYpSmgGExjvHyDFVKT35BQ6uP29rclI7staioO";
    public static final String IV = "fJwk3X0IAZVacziSbdj6xtCvgYD8lr7s5PL4eFuB2UTn1GpMhmRyqQHEON9WoK";

    private String permutation;
    private int position = 0;
    private HashMap<Character, Integer> map;

    public Rotor(String permutation) {
        if (permutation.length() != Alphabet.LENGTH) {
            throw new IllegalArgumentException("permutation size is not equal Alphabet size");
        }
        this.permutation = permutation;
        this.map = new HashMap<Character, Integer>();
        for (int i = 0; i < Alphabet.LENGTH; i++) {
            map.put(permutation.charAt(i), i);
        }
    }

    public static Rotor create() {
        return new Rotor(Alphabet.getRotorMap());
    }
    
    @Override
    public String toString() {
        return permutation + " " + String.valueOf(position);
    }

    public char execute(char input) {
        int pos = Alphabet.getPos(input) + position;
        pos = pos % Alphabet.LENGTH;
        return permutation.charAt(pos);
    }

    public char revert(char output) {
        int pos = map.get(output);
        pos = pos - position;
        if (pos < 0) {
            pos += Alphabet.LENGTH;
        }
        return Alphabet.revertPos(pos);
    }

    public void setPosition(int position) {
        this.position = Math.abs(position) % Alphabet.LENGTH;

    }

    public void setPosition(char ch) {
        setPosition(Alphabet.getPos(ch));
    }

    public void rotate() {
        position = (position + 1) % Alphabet.LENGTH;
    }
}
