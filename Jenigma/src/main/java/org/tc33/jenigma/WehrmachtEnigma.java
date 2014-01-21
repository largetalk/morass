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
 * 
 */
public class WehrmachtEnigma extends Enigma {
	
	private final static Rotor[] AVAILABLE_ROTORS = new Rotor[]{Rotor.ROTOR_I,
																Rotor.ROTOR_II,
																Rotor.ROTOR_III,
																Rotor.ROTOR_IV,
																Rotor.ROTOR_V};
	
	/**
	 * The WehrmachtEnigma takes 3 rotors from the set of rotors I-V
	 * @param plugboard
	 * @param rotor1 An integer between 1 and 5 inclusive to refer to one of 
	 * the 5 available rotors of the Wehrmacht enigma.
	 * @param rotor2 As for rotor1.
	 * @param rotor3 As for rotor1.
	 * @param startPositions A 3 element char array of starting positions, chars 'A'-'Z' are valid.
	 * @param reflector
	 */
	public WehrmachtEnigma(Plugboard plugboard, int rotor1, int rotor2, int rotor3, char[] startPositions, Reflector reflector) {
		super(new Rotor[]{AVAILABLE_ROTORS[rotor1-1], AVAILABLE_ROTORS[rotor2-1], AVAILABLE_ROTORS[rotor3-1]}, startPositions, plugboard, reflector);
	}
}
