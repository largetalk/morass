package com.adsame.adx.encrypt.enigma;

import com.adsame.adx.encrypt.enigma.components.Alphabet;

public class EnigmaStatus {
    private Alphabet alphabet;
    private int status[];
    private int steps[];


    public EnigmaStatus(Alphabet alphabet, int rotorNum) {
        this.alphabet = alphabet;
        status = new int[rotorNum];
        steps = new int[rotorNum];
    }

    public void setRotorPositions(byte[] positions) {
        if (status.length != positions.length) {
            throw new IllegalArgumentException("positions size is not equal rotors size");
        }
        for (int i = 0; i < status.length; i++) {
            if (alphabet.isValid(positions[i])) {
                status[i] = alphabet.getPos(positions[i]);
            } else {
                throw new IllegalArgumentException("setRotorPositions failed");
            }
        }
    }

    public int rotorStatus(int rotorSeq) {
        return status[rotorSeq];
    }

    public void rotate() {
        boolean flag = true;
        for (int i = 0; i < status.length; i++) {
            if (flag) {
                status[i] = (status[i] + 1) % alphabet.size();
                steps[i] ++;
                if (steps[i] == alphabet.size()) {
                    steps[i] = 0;
                    flag = true;
                } else {
                    flag = false;
                }
            }
        }

    }
}
