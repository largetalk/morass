/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt.enigma;

import com.adsame.adx.encrypt.enigma.components.Alphabet;
import com.adsame.adx.encrypt.enigma.components.Plugboard;
import com.adsame.adx.encrypt.enigma.components.Reflector;
import com.adsame.adx.encrypt.enigma.components.Rotor;

public class Enigma {

    private final Plugboard plugboard;
    private final Rotor[] rotors;
    private final int[] turns;
    private final Reflector reflector;

    public Enigma(Rotor[] rotors, Reflector reflector) {
        this(rotors, reflector, null);
    }

    public Enigma(Rotor[] rotors, Reflector reflector, Plugboard plugboard) {
        this.rotors = rotors;
        this.plugboard = plugboard;
        this.reflector = reflector;

        turns = new int[rotors.length];
        for (int i = 0; i < rotors.length; i++) {
            turns[i] = (int) Math.pow(Alphabet.LENGTH, i);
        }
    }

    public String execute(String input) {
        char inputChar, outputChar;
        StringBuilder output = new StringBuilder();

        int n = 0;
        for (int i = 0; i < input.length(); i++) {
            inputChar = input.charAt(i);
            if (Alphabet.isValid(inputChar)) {
                outputChar = inputChar;
                n++;

                if (plugboard != null) {
                    outputChar = plugboard.getSwappedChar(outputChar);
                }
                for (Rotor rotor : rotors) {
                    outputChar = rotor.execute(outputChar);
                }

                outputChar = reflector.getCipherChar(outputChar);

                // Put char back through the rotors.
                for (int j = rotors.length - 1; j >= 0; j--) {
                    outputChar = rotors[j].revert(outputChar);
                }
                
                if (plugboard != null) {
                    outputChar = plugboard.getSwappedChar(outputChar);
                }
                
                output = output.append(outputChar);

                // Rotate the rotors one position.
                rotateRotors(n);
            }
        }
        return output.toString();
    }

    public void rotateRotors(int charPos) {
        for (int i = 0; i < rotors.length; i++) {
            int rotorPos = charPos % turns[i];

            if (rotorPos == 0) {
                rotors[i].rotate();
            }
        }
    }

    public final void setRotorPositions(char[] positions) {
        if (rotors.length != positions.length) {
            throw new IllegalArgumentException("positions size is not equal rotors size");
        }
        
        for (int i = 0; i < positions.length; i++) {
            rotors[i].setPosition(positions[i]);
        }
    }
}
