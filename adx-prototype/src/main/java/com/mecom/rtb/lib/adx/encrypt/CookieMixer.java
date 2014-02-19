/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt;

import com.adsame.rtb.lib.adx.encrypt.enigma.Alphabet;
import com.adsame.rtb.lib.adx.encrypt.enigma.Enigma;
import com.adsame.rtb.lib.adx.encrypt.enigma.Enigma.EnigmaPlayer;
import com.adsame.rtb.lib.adx.encrypt.enigma.EnigmaBuilder;

public final class CookieMixer {

    private static final Alphabet ALPHABET;
    private static final Enigma ENIGMA;

    static {
        ALPHABET = EnigmaBuilder.DEFAULT_ALPHABET;
        EnigmaBuilder builder = new EnigmaBuilder(ALPHABET);

        ENIGMA = builder.addRotor(EnigmaBuilder.ROTOR_I)
                .addRotor(EnigmaBuilder.ROTOR_II)
                .addRotor(EnigmaBuilder.ROTOR_III)
                .addRotor(EnigmaBuilder.ROTOR_IV)
                .build(EnigmaBuilder.REFLECTOR);
    }

    public static String mix(String cookieId, String dspid) {
        byte[] dspChar = getHashChars(dspid.hashCode());
        byte[] asidChar = getHashChars(cookieId.hashCode());
        byte[] starts = new byte[]{dspChar[0], dspChar[1], asidChar[0], asidChar[1]};

        try {
            EnigmaPlayer player = ENIGMA.buildEnigmaPlayer();
            player.setRotorPositions(starts);
            byte[] encrypted = player.execute(cookieId.getBytes());
            return new String(starts) + new String(encrypted);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String demix(String encrypted) {
        byte[] starts = encrypted.substring(0, 4).getBytes();

        try {
            EnigmaPlayer player = ENIGMA.buildEnigmaPlayer();
            player.setRotorPositions(starts);
        return new String(player.execute(encrypted.substring(4).getBytes()));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static byte[] getHashChars(int hashCode) {
        byte[] hc = new byte[2];
        int high = (hashCode >>> 24) ^ ((hashCode >> 16) & 0xff);
        int low = ((hashCode >> 8) & 0xff) ^ (hashCode & 0xff);
        hc[0] = ALPHABET.getOuterFromInner(high);
        hc[1] = ALPHABET.getOuterFromInner(low);
        return hc;
    }
}
