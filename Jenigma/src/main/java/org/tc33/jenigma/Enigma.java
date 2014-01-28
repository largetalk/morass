/*
 *  Copyright 2003-2008 Tom Castle (tc33.org)
 *  Licensed under GNU General Public License
 *
 *  This file is part of JEnigma.
 *
 *  JEnigma is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JEnigma is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JEnigma.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tc33.jenigma;

import org.tc33.jenigma.components.*;

public class Enigma {

    // Components of Enigma machine.
    private Plugboard plugboard;
    private Rotor[] rotors;
    private int[] turns;
    private Reflector reflector;

    public Enigma(Rotor[] rotors, char[] startPositions, Plugboard plugboard, Reflector reflector) {
        configure(rotors, startPositions, plugboard, reflector);
    }

    public void configure(Rotor[] rotors, char[] startPositions, Plugboard plugboard, Reflector reflector) {
        // Setup the machine.
        this.rotors = rotors;
        setRotorPositions(startPositions);
        this.plugboard = plugboard;
        this.reflector = reflector;
        
        turns = new int[rotors.length];
        for (int i = 0; i < rotors.length; i++) {
            turns[i] = (int) Math.pow(Alphabet.length(), i);
        }
    }

    public String execute(String input) {

        char inputChar, outputChar;
        StringBuilder output = new StringBuilder();

        int n = 0;
        for (int i = 0; i < input.length(); i++) {
            inputChar = input.charAt(i);
            if (Alphabet.isValid(inputChar)) {
                n++;

                outputChar = plugboard.getSwappedChar(inputChar);
                for (int j = 0; j < rotors.length; j++) {
                    outputChar = rotors[j].execute(outputChar);
                }

                outputChar = reflector.getCipherChar(outputChar);

                // Put char back through the rotors.
                for (int j = rotors.length - 1; j >= 0; j--) {
                    outputChar = rotors[j].revert(outputChar);
                }
                outputChar = plugboard.getSwappedChar(outputChar);

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

    public void setRotorPositions(char[] positions) {
        for (int i = 0; i < positions.length; i++) {
            rotors[i].setPosition(positions[i]);
        }
    }

    public static void main(String[] args) {
        //test();
        benchmark();
    }

    public static void test() {
        Rotor rotor1 = new Rotor(Alphabet.getRotorMap());
        rotor1.setPosition(5);
        System.out.println(rotor1);
        char[] inputs = new char[]{'A', 'B', 'C', 'a', 'b', 'z', '0', '8', '9', 'B', 'C', 'D'};
        System.out.println(String.valueOf(inputs));
        StringBuilder outputs = new StringBuilder();
        for (char c: inputs) {
            outputs.append(rotor1.execute(c));
        }
        System.out.println(outputs.toString());
        rotor1.setPosition(5);
        for (char c: outputs.toString().toCharArray()) {
            System.out.print(rotor1.revert(c));
        }
        System.out.println("\n========");

        Reflector reflector1 = new Reflector(Alphabet.getReflectorMap());
        System.out.println(reflector1);
        char[] reflectorIns = new char[]{ 'B', 'C', 'i', 'b', 'y', '0', '8', '9', 'B', 'C'};
        System.out.println(String.valueOf(reflectorIns));
        StringBuilder reflectorOuts = new StringBuilder();
        for (char c: reflectorIns) {
            reflectorOuts.append(reflector1.getCipherChar(c));
        }
        System.out.println(reflectorOuts.toString());
        for (char c: reflectorOuts.toString().toCharArray()) {
            System.out.print(reflector1.getCipherChar(c));
        }
        System.out.println("\n========");

        Plugboard plugboard1 = new Plugboard();
        plugboard1.addCable('A', 'T').addCable('U', 'v');
        System.out.println("\n========");


        Rotor[] rotors = new Rotor[]{Rotor.create(), Rotor.create(), Rotor.create()};
        char[] startPositions = new char[]{'A', 'A', 'A'};
        Reflector reflector = Reflector.create();
        Plugboard plugboard = new Plugboard();
        plugboard.addCable('A', 'T').addCable('U', 'v');

        Enigma enigma = new Enigma(rotors, startPositions, plugboard, reflector);
        String plaintext = "VERYBIGQUESTIONthisisbeautifule9527";
        String ciphertext = enigma.execute(plaintext);
        System.out.println(plaintext);
        System.out.println(ciphertext);

        Enigma enigma1 = new Enigma(rotors, startPositions, plugboard, reflector);
        String replaintext = enigma1.execute(ciphertext);
        System.out.println(replaintext);

    }
    public static void benchmark()  {
        Rotor[] rotors = new Rotor[]{
            Rotor.create(), Rotor.create(), Rotor.create(), Rotor.create(),
//            Rotor.create(), Rotor.create(), Rotor.create(), Rotor.create(),
//            Rotor.create(), Rotor.create(), Rotor.create(), Rotor.create(),
//            Rotor.create(), Rotor.create(), Rotor.create(), Rotor.create()
        };
//        char[] startPositions = new char[]{
//            'A', 'B', 'C', 'D',
//            'E', 'F', 'G', 'h',
//            'a', 'b', 'c', 'g',
//            'e', 'f', 'g', 'i'
//        };
        Reflector reflector = Reflector.create();
        Plugboard plugboard = new Plugboard();
        plugboard.addCable('A', 'T').addCable('U', 'v');

        
        String plaintext = "VERYBIGQUESTIONthisisbeautifule9527";
        char[] startPositions = Alphabet.getStartPostion(plaintext.hashCode());
        //System.out.println(startPositions);
        Enigma enigma = new Enigma(rotors, startPositions, plugboard, reflector);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            enigma.setRotorPositions(startPositions);
            String ciphertext = enigma.execute(plaintext);

            enigma.setRotorPositions(startPositions);
            String tmp = enigma.execute(ciphertext);
            //System.out.println(tmp);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("run 100000 times used " + (endTime - startTime));
    }
}
