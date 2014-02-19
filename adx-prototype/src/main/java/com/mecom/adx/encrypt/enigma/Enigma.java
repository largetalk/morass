/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma;

import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;

public class Enigma {

    private Alphabet alphabet;
    private final Rotor[] rotors;
    private final Reflector reflector;

    public Enigma(Alphabet alphabet, Rotor[] rotors, Reflector reflector) {
        this.alphabet = alphabet;
        this.rotors = rotors;
        this.reflector = reflector;
    }

    public byte[] execute(byte[] input, EnigmaStatus enigmaStatus) {
        byte inputChar, outputChar;
        byte[] output = new byte[input.length];

        int n = 0;
        for (int i = 0; i < input.length; i++) {
            inputChar = input[i];
            if (alphabet.isValid(inputChar)) {
                outputChar = inputChar;

                for (int j = 0; j < rotors.length; j++) {
                    outputChar = rotors[j].execute(outputChar, enigmaStatus.rotorStatus(j));
                }

                outputChar = reflector.getCipherByte(outputChar);

                // Put char back through the rotors.
                for (int j = rotors.length - 1; j >= 0; j--) {
                    outputChar = rotors[j].revert(outputChar, enigmaStatus.rotorStatus(j));
                }

                output[n++] = outputChar;

                enigmaStatus.rotate();
            }
        }
        return output;
    }

    public final void setRotorPositions(byte[] positions, EnigmaStatus enigmaStatus) {
        enigmaStatus.setRotorPositions(positions);
    }

    public EnigmaStatus buildEnigmaStatus() {
        return new EnigmaStatus(alphabet, rotors.length);
    }
}
