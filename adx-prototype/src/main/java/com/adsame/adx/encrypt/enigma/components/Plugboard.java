/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma.components;

import java.util.Arrays;

public class Plugboard extends Permutator {

    public Plugboard(Alphabet alphabet, byte[] permutation) {
        super(alphabet, permutation);
    }
}
