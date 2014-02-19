/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import com.adsame.adx.encrypt.enigma.Enigma;
import com.adsame.adx.encrypt.enigma.EnigmaBuilder;
import com.adsame.adx.encrypt.enigma.EnigmaStatus;
import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;

public class CookieMixer {
    private static Enigma enigma;

    static {
        EnigmaBuilder builder = new EnigmaBuilder(EnigmaBuilder.DEFAULT_ALPHABET);

        enigma = builder.addRotor(EnigmaBuilder.ROTOR_I)
                .addRotor(EnigmaBuilder.ROTOR_II)
                .addRotor(EnigmaBuilder.ROTOR_III)
                .addRotor(EnigmaBuilder.ROTOR_IV)
                .addReflector(EnigmaBuilder.DEFAULT_RELECTOR)
                .build();
    }

    public static String mix(String cookieId, String dspid) {
        //byte[] dspChar = Alphabet.getHashChars(dspid.hashCode());
        //byte[] asidChar = Alphabet.getHashChars(cookieId.hashCode());
        //byte[] head = new byte[][]{dspChar[0], dspChar[1], asidChar[0], asidChar[1]};


        EnigmaStatus enigmaStatus = enigma.buildEnigmaStatus();
        byte[] mima = "aaaa".getBytes();
        enigma.setRotorPositions(mima, enigmaStatus);
        byte[] encrypted = enigma.execute(cookieId.getBytes(), enigmaStatus);
        return new String(encrypted);
    }

    public static String demix(String encrypted) {
        //char[] starts = encrypted.substring(0, 4).toCharArray();

        EnigmaStatus enigmaStatus = enigma.buildEnigmaStatus();

        byte[] mima = "aaaa".getBytes();
        enigma.setRotorPositions(mima, enigmaStatus);
        return new String(enigma.execute(encrypted.getBytes(), enigmaStatus));
    }

    public static void main(String[] args) {

        String plain = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String mixed = mix(plain, "a");
        String demixed = demix(mixed);
        System.out.println(plain);
        System.out.println(mixed);
        System.out.println(demixed);
    }

}
