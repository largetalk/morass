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


import java.util.HashMap;

public class Rotor extends Permutator {

	private String permutation;
        private HashMap<Character, Integer> map =  new HashMap<Character, Integer>();
	
	private int  position = 0;

    public Rotor(String permutation) {
        if (permutation.length() != Alphabet.LENGTH) return;
        this.permutation = permutation;
        for (int i=0; i<Alphabet.LENGTH; i++) map.put(permutation.charAt(i), i);
    }
    
    public static Rotor create() {
        return new Rotor(Alphabet.getRotorMap());
    }

    @Override
    public String toString() {
        return permutation +  " " + String.valueOf(position);
    }

    public char execute(char input) {
        int pos = Alphabet.getPos(input) + position;
        pos = pos % Alphabet.LENGTH;
        return permutation.charAt(pos);
    }

    public char revert(char output) {
        int pos = map.get(output);
        pos = pos  - position;
        if (pos<0) pos += Alphabet.LENGTH;
        return Alphabet.revertPos(pos);
    }

    public void setPosition(int position) {
    	this.position = Math.abs(position) % Alphabet.LENGTH;

    }
    public void setPosition(char ch) {
        setPosition(Alphabet.getPos(ch));
    }
    
    public void rotate() {
        position = (position + 1) % Alphabet.LENGTH;
    }

}
