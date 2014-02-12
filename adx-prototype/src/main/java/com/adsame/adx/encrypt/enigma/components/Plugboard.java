/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.Arrays;

public class Plugboard extends Permutator {

    private final char[] cables = new char[Alphabet.LENGTH];

    public Plugboard() {}

    public Plugboard addCable(char cableEnd1, char cableEnd2) {
        cables[Alphabet.getPos(cableEnd1)] = cableEnd2;
        cables[Alphabet.getPos(cableEnd2)] = cableEnd1;
        return this;
    }

    public char getSwappedChar(char cableEnd) {
        char otherEnd = cables[Alphabet.getPos(cableEnd)];
        if (otherEnd == '\u0000') {
            return cableEnd;
        }

        return otherEnd;
    }

    public void clearCables() {
        Arrays.fill(cables, '\u0000');
    }

}
