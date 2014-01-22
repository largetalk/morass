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


    String reflectorKey;
    
    /**
     * Constructor.
     */
    public Reflector(String permutation) {
        this.reflectorKey = permutation;
    }

    public static Reflector create() {
        return new Reflector(Alphabet.getReflectorMap());
    }

    @Override
    public String toString() {
        return reflectorKey;
    }

    /**
     * Get the cipher char from the received plain char.
     */
    public char getCipherChar(char plainChar) {
        int pos = Alphabet.getPos(plainChar);
        char cipherChar = reflectorKey.charAt(pos);
        return cipherChar;
    }

}
