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

import com.google.common.collect.HashBiMap;



/**
 * Scrambler component of Enigma machine.
 */
public class Rotor extends Permutator {

	private String permutation;
        private int table_length;
        private HashBiMap<Character, Integer> table =  HashBiMap.create();
	
	private int  position = 0;

    /**
     * Constructor for rotor, taking parameters to show how to encrypt when set at position 'A'.
     */
    public Rotor(String permutation) {
        this.permutation = permutation;
        this.table_length = permutation.length();
        for (int i=0; i<table_length; i++) table.put(permutation.charAt(i), i);

    }
    public static Rotor create() {
        return new Rotor(Alphabet.getRotorMap());
    }

    @Override
    public String toString() {
        return permutation +  " " + String.valueOf(position);
    }

    /**
     * Get the cipher char from the recieved plain char.
     */
    public char execute(char input) {
        int pos = Alphabet.getPos(input) + position;
        pos = pos % table_length;
        return permutation.charAt(pos);
    }

    /**
     * Get the plain char from the recieved cipher char.
     */
    public char revert(char output) {
        int pos = table.get(output);
        pos = pos - position;
        if (pos<0) pos += table_length;
        return Alphabet.revertPos(pos);
    }

    /**
     * Set this rotor to a new position.
     * @param position
     */
    public void setPosition(int position) {
    	// How many positions do we need to rotate?
    	this.position = Math.abs(position) % Alphabet.length();

    }
    public void setPosition(char ch) {
        setPosition(Alphabet.getPos(ch));
    }
    
    /**
     * Rotate the rotor one position.
     */
    public void rotate() {
    	// Increment position by one, bending round to the 'A' at 'Z' NIFQFHKBUGQRAFRU
        position++;
        if (position > Alphabet.length()) position=0;


    }
    

    

    
    /**
     * Prints the whole key for this rotor - for testing purposes.
     */
    public void printKey() {
        System.out.println("PT................CT - CT................PT");
        System.out.println(permutation);

    }
}
