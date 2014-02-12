/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import com.adsame.adx.encrypt.enigma.Enigma;
import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;

public class CookieMixer {

    public static String mix(String cookieId, String dspid) {
        char[] dspChar = Alphabet.getHashChars(dspid.hashCode());
        char[] asidChar = Alphabet.getHashChars(cookieId.hashCode());
        char[] head = new char[]{dspChar[0], dspChar[1], asidChar[0], asidChar[1]};
        
        Enigma enigma = new Enigma(createDefaultRotors(), Reflector.DEFAULT);
        enigma.setRotorPositions(head);
        String encrypted = enigma.execute(cookieId);
        return new String(head) + encrypted;
    }

    public static String demix(String encrypted) {
        char[] starts = encrypted.substring(0, 4).toCharArray();
        Enigma enigma = new Enigma(createDefaultRotors(), Reflector.DEFAULT);
        enigma.setRotorPositions(starts);
        return enigma.execute(encrypted.substring(4));
    }
    
    private static Rotor[] createDefaultRotors() {
        return new Rotor[]{
            new Rotor(Rotor.I),
            new Rotor(Rotor.II),
            new Rotor(Rotor.III),
            new Rotor(Rotor.IV)
        };
    }
}
