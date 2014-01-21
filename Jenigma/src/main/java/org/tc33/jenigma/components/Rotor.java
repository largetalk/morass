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

package org.tc33.jenigma.components;



/**
 * Scrambler component of Enigma machine.
 */
public class Rotor extends Permutator {

	public static final Rotor ROTOR_I = new Rotor(new char[]{'E','K','M','F','L','G','D','Q','V','Z','N','T','O','W','Y','H','X','U','S','P','A','I','B','R','C','J'});
	public static final Rotor ROTOR_II = new Rotor(new char[]{'A','J','D','K','S','I','R','U','X','B','L','H','W','T','M','C','Q','G','Z','N','P','Y','F','V','O','E'});
	public static final Rotor ROTOR_III = new Rotor(new char[]{'B','D','F','H','J','L','C','P','R','T','X','V','Z','N','Y','E','I','W','G','A','K','M','U','S','Q','O'});
	public static final Rotor ROTOR_IV = new Rotor(new char[]{'E','S','O','V','P','Z','J','A','Y','Q','U','I','R','H','X','L','N','F','T','G','K','D','C','M','W','B'});
	public static final Rotor ROTOR_V = new Rotor(new char[]{'V','Z','B','R','G','I','T','Y','U','P','S','D','N','H','L','X','A','W','M','J','Q','O','F','E','C','K'});
	public static final Rotor ROTOR_VI = new Rotor(new char[]{'J','P','G','V','O','U','M','F','Y','Q','B','E','N','H','Z','R','D','K','A','S','X','L','I','C','T','W'});
	public static final Rotor ROTOR_VII = new Rotor(new char[]{'N','Z','J','H','G','R','C','X','M','Y','S','W','B','O','U','F','A','I','V','L','P','E','K','Q','D','T'});
	public static final Rotor ROTOR_VIII = new Rotor(new char[]{'F','K','Q','H','T','L','X','O','C','B','J','S','P','D','Z','R','A','M','E','W','N','I','U','Y','G','V'});
	public static final Rotor ROTOR_BETA = new Rotor(new char[]{'L','E','Y','J','V','C','N','I','X','W','P','B','Q','M','D','R','T','A','K','Z','G','F','U','H','O','S'});
	public static final Rotor ROTOR_GAMMA = new Rotor(new char[]{'F','S','O','K','A','N','U','E','R','H','M','B','T','I','Y','C','W','L','Q','P','Z','X','V','G','J','D'});

	char[] permutation = new char[26];
	char[] invertedPermutation = new char[26];
	
	private char position = 'A';

    /**
     * Constructor for rotor, taking parameters to show how to encrypt when set at position 'A'.
     */
    public Rotor(char[] permutation) {
        this.permutation = permutation;
        
        // Keep an inverted version of the permutation.
        createDecryptionKey();
    }

    /**
     * Get the cipher char from the recieved plain char.
     */
    public char execute(char input) {
        int pos = (int) input;
        pos -= 65; //convert ascii to 0-25
        return permutation[pos];
    }

    /**
     * Get the plain char from the recieved cipher char.
     */
    public char revert(char output) {
        int pos = (int) output;
        pos -= 65; // convert ascii to 0-25
        return invertedPermutation[pos];
    }

    /**
     * Set this rotor to a new position.
     * @param position
     */
    public void setPosition(char position) {
    	// How many positions do we need to rotate?
    	int rotations = position - this.position;
    	rotations = (26 + rotations) % 26;
    	
    	// Rotate that many times.
    	for (int i=0; i<rotations; i++) {
    		rotate();
    	}
    }
    
    /**
     * Rotate the rotor one position.
     */
    public void rotate() {
    	// Increment position by one, bending round to the 'A' at 'Z' NIFQFHKBUGQRAFRU
    	if (++position > 'Z') 
    		position = 'A';
    	
    	// Increment each char in permutation by one.
    	for (int i=0; i<permutation.length; i++) {
        	if (++permutation[i] > 'Z') 
        		permutation[i] = 'A';
    	}
    	
    	// Move each char in permutation one place to the right.
    	char tmp = permutation[permutation.length-1];
    	for (int i=permutation.length-1; i>0; i--) {
    		permutation[i] = permutation[i-1];
    	}
    	permutation[0] = tmp;
    	
    	createDecryptionKey();
    }
    
    /**
     * Creates decryption key from the encryption key.
     */
    public void createDecryptionKey() {
        for(int i=0; i < permutation.length; i++){
            invertedPermutation[permutation[i]-65] = (char)(i + 65);
        }
    }
    
    /**
     * Change the wiring of the rotor.
     */
    public void setPermutation(char[] permutation) {
        this.permutation = permutation;
        //initialPermutation = permutation.clone();
    }
    
    /**
     * Prints the whole key for this rotor - for testing purposes.
     */
    public void printKey() {
        System.out.println("PT................CT - CT................PT");
        for (int i=0; i < permutation.length; i++){
            System.out.println(i + ".................." + permutation[i] + " - " + i + ".................." + invertedPermutation[i]);
        }
    }
}
