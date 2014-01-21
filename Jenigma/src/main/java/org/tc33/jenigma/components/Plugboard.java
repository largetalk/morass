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

/**
 * Plugboard component of Engima machine.
 */
public class Plugboard extends Permutator {

	private char[] cables = new char[26];
    
    /**
     * Constructor. This is commented out as it's safer to force users to use 
     * the addCable method.
     */
    /*public Plugboard(char[] cables) {
        this.cables = cables;
    }*/
    
    /**
     * Create an empty plugboard to add cables to later.
     */
    public Plugboard() {}
    
    /**
     * Add a cable to the plugboard.
     * @param cableEnd1
     * @param cableEnd2
     */
	public void addCable(char cableEnd1, char cableEnd2) {
		cableEnd1 = Character.toUpperCase(cableEnd1);
		cableEnd2 = Character.toUpperCase(cableEnd2);
		
		cables[cableEnd1-65] = cableEnd2;
		cables[cableEnd2-65] = cableEnd1;
	}
	
	public char getSwappedChar(char cableEnd) {
		cableEnd = Character.toUpperCase(cableEnd);
		char otherEnd = cables[cableEnd-65];
		if (otherEnd == '\u0000')
			otherEnd = cableEnd;
		
		return otherEnd;
	}
	
	public void clearCables() {
		Arrays.fill(cables, '\u0000');
	}
    
	/*public void setCables(char[] cables) {
		this.cables = cables;
	}*/
	
    /**
     * Prints the whole key for the plugboard - for testing purposes.
     */
    public void printKey() {
        System.out.println("PT................CT");
        for (int i=0; i < cables.length; i++){
            System.out.println((char)(i+65) + ".................." + cables[i]);
        }
    }
}
