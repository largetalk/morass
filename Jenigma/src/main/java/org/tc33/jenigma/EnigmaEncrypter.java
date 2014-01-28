/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tc33.jenigma;

import org.tc33.jenigma.components.Alphabet;
import org.tc33.jenigma.components.Plugboard;
import org.tc33.jenigma.components.Reflector;
import org.tc33.jenigma.components.Rotor;

/**
 *
 * @author adsame
 */
public class EnigmaEncrypter {
    private static Rotor[] ROTORS;
    private static Reflector REFLECTOR;
    private static Plugboard PLUGBOARD;
    private static Enigma enigma;
    
    
    static {
        ROTORS = new Rotor[]{
            new Rotor("JqBDko4fZyzt1Yr6GRMFxOUIalXPnELNcs823hAvKH905gdmp7CTVbuQeiWSwj"),
            new Rotor("LbzPDGJU3nf81OV5BHqCrQcAlpohFm7aSjdMRKEvXNg6s0T4ZWty2w9YiIeuxk"),
            new Rotor("bWhqJ1AeX8kzfMCRN0dUZL4wnYpSmgGExjvHyDFVKT35BQ6uP29rclI7staioO"),
            new Rotor("fJwk3X0IAZVacziSbdj6xtCvgYD8lr7s5PL4eFuB2UTn1GpMhmRyqQHEON9WoK")
        };
        PLUGBOARD = new Plugboard();
        PLUGBOARD.addCable('A', 'T').addCable('U', 'v');
        REFLECTOR = new Reflector("uT6SUYHGgvN5jKX0kbDBErpOFteRh1a8IcnMQq2i9WlV4ZAJy3w7PdmxsLCzfo");
        enigma = new Enigma(ROTORS, 
                new char[]{'0','0','0','0'},
                PLUGBOARD,
                REFLECTOR
        );
    }
        
    public static String encrypt(String cookieId, String dspid) {
        char[] dspChar = Alphabet.getHashChars(dspid.hashCode());
        char[] asidChar = Alphabet.getHashChars(cookieId.hashCode());
        char[] starts = new char[]{dspChar[0], dspChar[1], asidChar[0], asidChar[1]};
        enigma.setRotorPositions(starts);
        String encrypted = enigma.execute(cookieId);
        return new String(starts) + encrypted;        
    }
    
    public static String decrypt(String encrypted) {
        char[] starts = encrypted.substring(0, 4).toCharArray();
        enigma.setRotorPositions(starts);
        return enigma.execute(encrypted.substring(4));
    }
    
    public static void main(String[] args) {
        benchmark();
    }
    public static void test() {
        String plaintext = "VERYBIGQUESTIONthisisbeautifule9527";
        String dspid = "mediav";
        String ciphertext = encrypt(plaintext, dspid);
        System.out.println(plaintext);
        System.out.println(ciphertext); 
        String replaintext = decrypt(ciphertext);
        System.out.println(replaintext);
    }
    
    public static void benchmark() {
        String plaintext = "VERYBIGQUESTIONthisisbeautifule9527";
        String dspid = "mediav";
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
        String ciphertext = encrypt(plaintext, dspid);
        String replaintext = decrypt(ciphertext);
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

    }
    
}
