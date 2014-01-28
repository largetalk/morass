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

import java.util.*;

public class Plugboard extends Permutator {

    private char[] cables = new char[Alphabet.length()];

    public Plugboard() {}

    public Plugboard addCable(char cableEnd1, char cableEnd2) {
        cables[Alphabet.getPos(cableEnd1)] = cableEnd2;
        cables[Alphabet.getPos(cableEnd2)] = cableEnd1;
        return this;
    }

    public char getSwappedChar(char cableEnd) {
        char otherEnd = cables[Alphabet.getPos(cableEnd)];
        if (otherEnd == '\u0000') {
            return cableEnd;
        }

        return otherEnd;
    }

    public void clearCables() {
        Arrays.fill(cables, '\u0000');
    }

}
