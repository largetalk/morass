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
package org.tc33.jenigma.test;

import org.tc33.jenigma.*;
import org.tc33.jenigma.components.*;

/**
 * Just some rudimentary tests, these will need much more work later.
 * 
 * Test #1 is likely to fail any time the configuration is changed because it
 * has a hardcoded expected ciphertext. Test #2 is more useful as it checks you 
 * can decrypt back to the same plaintext.
 */
public class TestRunner {
	
	public static void main(String[] args) {
/*
		Enigma enigma = new Enigma(new Rotor[]{Rotor.ROTOR_I, Rotor.ROTOR_II, Rotor.ROTOR_III}, 
								   new char[]{'A','A','A'},
								   new Plugboard(),
								   Reflector.REFLECTOR_B);
		
		String expectedPlaintext = "HELLOCRYPTOWORLD";
		String expectedCiphertext = "EAEEZOWWLRLPIHQO";
		
		String ciphertext = enigma.execute(expectedPlaintext);
		enigma.setRotorPositions(new char[]{'A','A','A'});
		String plaintext = enigma.execute(ciphertext);
	
		boolean test1 = ciphertext.equals(expectedCiphertext);
		boolean test2 = plaintext.equals(expectedPlaintext);
		
		System.out.println("TEST #1: " + (test1 ? "PASSED" : "FAILED"));
		System.out.println("TEST #2: " + (test2 ? "PASSED" : "FAILED"));
		System.out.println("-------------------");
		System.out.println("OVERALL: " + ((test1 && test2) ? "PASSED" : "FAILED"));*/
	}
}
