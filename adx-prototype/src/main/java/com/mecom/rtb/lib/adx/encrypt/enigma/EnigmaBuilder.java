/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma;

import com.adsame.rtb.lib.adx.encrypt.enigma.components.Reflector;
import com.adsame.rtb.lib.adx.encrypt.enigma.components.Rotor;
import java.util.ArrayList;

public class EnigmaBuilder {

    public static Alphabet DEFAULT_ALPHABET = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".getBytes());
    public static byte[] ROTOR_I = "JqBDko4fZyzt1Yr6GRMFxOUIalXPnELNcs823hAvKH905gdmp7CTVbuQeiWSwj".getBytes();
    public static byte[] ROTOR_II = "LbzPDGJU3nf81OV5BHqCrQcAlpohFm7aSjdMRKEvXNg6s0T4ZWty2w9YiIeuxk".getBytes();
    public static byte[] ROTOR_III = "bWhqJ1AeX8kzfMCRN0dUZL4wnYpSmgGExjvHyDFVKT35BQ6uP29rclI7staioO".getBytes();
    public static byte[] ROTOR_IV = "fJwk3X0IAZVacziSbdj6xtCvgYD8lr7s5PL4eFuB2UTn1GpMhmRyqQHEON9WoK".getBytes();
    public static byte[] REFLECTOR = "lXhYGoAiLQ6qVSHZ4EfWU8zspn9rDkKCJ13FbawgjId27eT0tP5OBRNmyxcvuM".getBytes();

    private final Alphabet alphabet;
    private final ArrayList<Rotor> rotors = new ArrayList<Rotor>();
    private Reflector reflector;

    public EnigmaBuilder(byte charSet[]) {
        this(new Alphabet(charSet));
    }

    public EnigmaBuilder(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public EnigmaBuilder addRotor(byte permutation[]) {
        int inner[] = transToInner(permutation);
        Rotor rotor = new Rotor(alphabet.size(), inner);
        rotors.add(rotor);
        return this;
    }

    public Enigma build(byte permutation[]) {
        int inner[] = transToInner(permutation);
        reflector = new Reflector(alphabet.size(), inner);
        return new Enigma(alphabet, rotors.toArray(new Rotor[rotors.size()]), reflector);
    }

    private int[] transToInner(byte permutation[]) {
        int inner[] = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            inner[i] = alphabet.getInnerFromOuter(permutation[i]);
        }
        return inner;
    }
}
