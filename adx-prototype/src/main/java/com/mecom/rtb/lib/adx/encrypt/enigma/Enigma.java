/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt.enigma;

import com.adsame.rtb.lib.adx.encrypt.enigma.components.Reflector;
import com.adsame.rtb.lib.adx.encrypt.enigma.components.Rotor;

public class Enigma {

    private final Alphabet alphabet;
    private final Rotor[] rotors;
    private final Reflector reflector;

    Enigma(Alphabet alphabet, Rotor rotors[], Reflector reflector) {
        this.alphabet = alphabet;
        this.rotors = rotors;
        this.reflector = reflector;
    }

    public class EnigmaPlayer {

        private final int status[];
        private final int startPos[];

        private EnigmaPlayer(int numRotor) {
            status = new int[numRotor];
            startPos = new int[numRotor];
        }

        public void setRotorPositions(byte positions[]) {
            if (status.length != positions.length) {
                throw new IllegalArgumentException("positions size is not equal rotors size");
            }
            for (int i = 0; i < status.length; i++) {

                if (alphabet.isValidByte(positions[i])) {
                    status[i] = alphabet.getInnerFromOuter(positions[i]);
                    startPos[i] = status[i];
                } else {
                    throw new IllegalArgumentException("setRotorPositions failed");
                }
            }
        }

        public byte[] execute(byte[] input) {
            int inInnerCode, outInnerCode;
            byte output[] = new byte[input.length];

            int n = 0;
            for (int i = 0; i < input.length; i++) {
                if (alphabet.isValidByte(input[i])) {
                    inInnerCode = alphabet.getInnerFromOuter(input[i]);
                    outInnerCode = executeOne(inInnerCode, status);
                    output[n++] = alphabet.getOuterFromInner(outInnerCode);

                    rotate();
                } else {
                    throw new IllegalArgumentException("error input");
                }
            }
            return output;
        }

        private void rotate() {
            for (int i = 0; i < status.length; i++) {
                status[i] = (status[i] + 1) % alphabet.size();
                if (status[i] != startPos[i]) {
                    break;
                }
            }
        }
    }

    public EnigmaPlayer buildEnigmaPlayer() {
        return new EnigmaPlayer(rotors.length);
    }

    public int rotorSize() {
        return rotors.length;
    }

    private int executeOne(int innerCode, int status[]) {
        for (int j = 0; j < rotors.length; j++) {
            innerCode = rotors[j].execute(innerCode, status[j]);
        }

        innerCode = reflector.getCipherCode(innerCode);

        // Put char back through the rotors.
        for (int j = rotors.length - 1; j >= 0; j--) {
            innerCode = rotors[j].revert(innerCode, status[j]);
        }
        return innerCode;
    }
}
