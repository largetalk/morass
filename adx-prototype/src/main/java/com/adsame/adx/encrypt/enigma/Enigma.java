/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma;

import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;

public class Enigma {

    private final Rotor[] rotors;
    private final int[] turns;
    private final Reflector reflector;

    public Enigma(Rotor[] rotors, Reflector reflector) {
        this.rotors = rotors;
        this.reflector = reflector;

        turns = new int[rotors.length];
        for (int i = 0; i < rotors.length; i++) {
            turns[i] = (int) Math.pow(reflector.getAlphabet().size(), i);
        }
    }

    public byte[] execute(byte[] input) {
        byte inputChar, outputChar;
        byte[] output = new byte[input.length];

        int n = 0;
        for (int i = 0; i < input.length; i++) {
            inputChar = input[i];
            if (reflector.getAlphabet().isValid(inputChar)) {
                outputChar = inputChar;

                for (Rotor rotor : rotors) {
                    outputChar = rotor.execute(outputChar);
                }

                outputChar = reflector.getCipherByte(outputChar);

                // Put char back through the rotors.
                for (int j = rotors.length - 1; j >= 0; j--) {
                    outputChar = rotors[j].revert(outputChar);
                }

                output[n++] = outputChar;

                // Rotate the rotors one position.
                rotateRotors(n);
            }
        }
        return output;
    }

    public void rotateRotors(int charPos) {
        for (int i = 0; i < rotors.length; i++) {
            int rotorPos = charPos % turns[i];

            if (rotorPos == 0) {
                rotors[i].rotate();
            }
        }
    }

    public final void setRotorPositions(byte[] positions) {
        if (rotors.length != positions.length) {
            throw new IllegalArgumentException("positions size is not equal rotors size");
        }
        
        for (int i = 0; i < positions.length; i++) {
            rotors[i].setPosition(positions[i]);
        }
    }
}
