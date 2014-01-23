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

/**
 * Replicates the encipherment of the Enigma machine.
 */
public class Enigma {

    // Components of Enigma machine.
    private Plugboard plugboard;
    private Rotor[] rotors;
    private Reflector reflector;

    /**
     * Sets up an enigma machine with rotors, a plugboard and a reflector.
     *
     * @param rotors          The set of rotors to be used in the order they should be used.
     * @param rotorPositions  The starting positions of the rotors. This should be
     *                        the same length as the rotors array and each char is the char that the rotor
     *                        in that position should be set to.
     * @param plugboardCables This should be an array of the length of the alphabet,
     *                        which lists pairings. It is vital that both ends of a cable are attached -
     *                        that is, if the first element is 'D' then the 4th element must be 'A' so that
     *                        A and D are swapped.
     * @param reflector       The reflector object to use.
     */
    public Enigma(Rotor[] rotors, char[] startPositions, Plugboard plugboard, Reflector reflector) {
        configure(rotors, startPositions, plugboard, reflector);
    }

    /**
     * Sets up an enigma machine with just rotors.
     *
     * @param rotors         The set of rotors to be used in the order they should be used.
     * @param rotorPositions The starting positions of the rotors. This should be
     *                       the same length as the rotors array and each char is the char that the rotor
     *                       in that position should be set to.
     */
    public Enigma(Rotor[] rotors, char[] startPositions) {
        configure(rotors, startPositions, new Plugboard(), null);
    }

    /**
     * Reconfigure this enigma machine with the given set of components.
     *
     * @param rotors          The set of rotors to be used in the order they should be used.
     * @param rotorPositions  The starting positions of the rotors. This should be
     *                        the same length as the rotors array and each char is the char that the rotor
     *                        in that position should be set to.
     * @param plugboardCables This should be an array of the length of the alphabet,
     *                        which lists pairings. It is vital that both ends of a cable are attached -
     *                        that is, if the first element is 'D' then the 4th element must be 'A' so that
     *                        A and D are swapped.
     * @param reflector       The reflector object to use.
     */
    public void configure(Rotor[] rotors, char[] startPositions, Plugboard plugboard, Reflector reflector) {
        // Setup the machine.
        this.rotors = rotors;
        setRotorPositions(startPositions);
        this.plugboard = plugboard;
        this.reflector = reflector;
    }

    /**
     * Encryption and decryption are performed in the same way in the Enigma. This
     * method should be used for both.
     */
    public String execute(String input) {
        // Perform operation.
        char inputChar, outputChar;
        StringBuilder output = new StringBuilder();
        //to send the correct char position excluding spaces (i inc. spaces)
        int n = 0;
        for (int i = 0; i < input.length(); i++) {
            inputChar = input.charAt(i);
            if (Alphabet.isValid(inputChar)) {
                n++;

                // Put char through the plugboard.
                outputChar = plugboard.getSwappedChar(inputChar);

                // Put char through the rotors.
                for (int j = 0; j < rotors.length; j++) {
                    outputChar = rotors[j].execute(outputChar);
                }

                // Put char through the reflector.
                if (reflector != null) {
                    outputChar = reflector.getCipherChar(outputChar);

                    // Put char back through the rotors.
                    for (int j = rotors.length - 1; j >= 0; j--) {
                        outputChar = rotors[j].revert(outputChar);
                    }

                    // Put char back through the plugboard.
                    outputChar = plugboard.getSwappedChar(outputChar);
                }

                // Add the char to the output.
                output = output.append(outputChar);

                // Rotate the rotors one position.
                rotateRotors(n);
            }
        }

        return output.toString();
    }

    /**
     * Rotate scramblers one position.
     */
    public void rotateRotors(int charPos) {
        for (int i = 0; i < rotors.length; i++) {
            // This is the number of rotations the smallest rotor has to make for one rotation here.
            int turns = (int) Math.pow(Alphabet.length(), i);

            // How many rotations has the smallest rotor made since we last turned?
            int rotorPos = charPos % turns;

            // Only rotate if smaller rotors have completed a full revolution.
            if (rotorPos == 0) {
                rotors[i].rotate();
            }
        }
    }

    /**
     * Set the position of the rotors.
     */
    public void setRotorPositions(char[] positions) {
        if (positions.length != rotors.length) {
            //TODO Throw some sort of exception.
        }

        for (int i = 0; i < positions.length; i++) {
            rotors[i].setPosition(positions[i]);
        }
    }

    public static void main(String[] args) {
        System.out.println(Alphabet.getRotorMap());
        System.out.println(Alphabet.table());
        System.out.println(Alphabet.getReflectorMap());
        System.out.println("========");

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
        plugboard1.printKey();
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
}
