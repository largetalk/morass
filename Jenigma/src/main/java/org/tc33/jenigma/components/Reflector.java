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
 * Reflector component of Enigma machine.
 */
public class Reflector extends Permutator {

	public static final Reflector REFLECTOR_DUMMY = new Reflector(new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'});
	public static final Reflector REFLECTOR_B = new Reflector(new char[]{'Y','R','U','H','Q','S','L','D','P','X','N','G','O','K','M','I','E','B','F','Z','C','W','V','J','A','T'});
	public static final Reflector REFLECTOR_C = new Reflector(new char[]{'F','V','P','J','I','A','O','Y','E','D','R','Z','X','W','G','C','T','K','U','Q','S','B','N','M','H','L'});
	public static final Reflector REFLECTOR_B_DUNN = new Reflector(new char[]{'E','N','K','Q','A','U','Y','W','J','I','C','O','P','M','L','M','D','X','Z','V','F','T','H','R','G','S'});
	public static final Reflector REFLECTOR_C_DUNN = new Reflector(new char[]{'R','D','O','B','J','N','T','K','V','E','H','M','L','F','C','W','Z','A','X','G','Y','I','P','S','U','Q'});
	
    char[] reflectorKey = new char[26];
    
    /**
     * Constructor.
     */
    public Reflector(char[] permutation) {
        this.reflectorKey = permutation;
    }

    /**
     * Get the cipher char from the received plain char.
     */
    public char getCipherChar(char plainChar) {
        int pos = (int) plainChar;
        pos -= 65; //convert ascii to 0-25
        char cipherChar = reflectorKey[pos];
        return cipherChar;
    }
    
     /**
     * Prints the whole key for the reflector - for testing purposes.
     */
    public void printKey() {
        System.out.println("PT................CT");
        for (int i=0; i < reflectorKey.length; i++){
            System.out.println(i + ".................." + reflectorKey[i]);
        }
    }
}
