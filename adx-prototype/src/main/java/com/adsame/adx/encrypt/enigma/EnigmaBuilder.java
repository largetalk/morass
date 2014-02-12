package com.adsame.adx.encrypt.enigma;

import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;
import java.util.ArrayList;

public class EnigmaBuilder {

    private Alphabet alphabet;
    private ArrayList<Rotor> rotors = new ArrayList<Rotor>();
    private Reflector reflector;

    public EnigmaBuilder(byte[] charSet) {
        this.alphabet = new Alphabet(charSet);
    }

    public EnigmaBuilder addRotor(byte[] permutation) {
        Rotor rotor = new Rotor(alphabet, permutation);
        rotors.add(rotor);
        return this;
    }

    public EnigmaBuilder addReflector(byte[] permutation) {
        reflector = new Reflector(alphabet, permutation);
        return this;
    }

    public Enigma build() {
        return new Enigma(rotors.toArray(new Rotor[rotors.size()]), reflector);
    }

    public String randomRelector() {
        return new String(this.alphabet.getReflectorMap());
    }
    public String randomRotor() {
        return new String(this.alphabet.getRotorMap());
    }

    public static byte[] DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".getBytes();
    public static byte[] ROTOR_I = "JqBDko4fZyzt1Yr6GRMFxOUIalXPnELNcs823hAvKH905gdmp7CTVbuQeiWSwj".getBytes();
    public static byte[] ROTOR_II = "LbzPDGJU3nf81OV5BHqCrQcAlpohFm7aSjdMRKEvXNg6s0T4ZWty2w9YiIeuxk".getBytes();
    public static byte[] ROTOR_III = "bWhqJ1AeX8kzfMCRN0dUZL4wnYpSmgGExjvHyDFVKT35BQ6uP29rclI7staioO".getBytes();
    public static byte[] ROTOR_IV = "fJwk3X0IAZVacziSbdj6xtCvgYD8lr7s5PL4eFuB2UTn1GpMhmRyqQHEON9WoK".getBytes();
    public static byte[] DEFAULT_RELECTOR = "lXhYGoAiLQ6qVSHZ4EfWU8zspn9rDkKCJ13FbawgjId27eT0tP5OBRNmyxcvuM".getBytes();
}
