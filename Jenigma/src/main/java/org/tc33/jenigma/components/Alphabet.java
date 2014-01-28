package org.tc33.jenigma.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public final class Alphabet {
    private final static String KEYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final static HashMap<Character, Integer> ALPHABET =  new HashMap<Character, Integer>();
    public final static int LENGTH;

    static {
        LENGTH = KEYS.length();
        for (int i=0; i<KEYS.length(); i++) ALPHABET.put(KEYS.charAt(i), i);
    }

    private Alphabet() { }

    public static int length() {
        return LENGTH;
    }

    public static String table() {
        return KEYS;
    }

    public static boolean isValid(char c) {
        return ALPHABET.containsKey(c);
    }

    public static int getPos(char c) {
        return ALPHABET.get(c);
    }

    public static char revertPos(int pos) {
        return KEYS.charAt(pos % LENGTH);
    }
    
    public static char[] getStartPostion(int hashCode) {
        char[] starts = new char[2];
        starts[0] = revertPos((int) (hashCode >>> 24));
        starts[1] = revertPos((int) ((hashCode >> 16) & 0xff));
        starts[2] = revertPos((int) ((hashCode >> 8) & 0xff));
        starts[3] = revertPos((int) (hashCode & 0xff));
        return starts;
    }
    public static char[] getHashChars(int hashCode) {
        char[] hc = new char[2];
        hc[0] = revertPos((int) (hashCode >>> 24) ^ ((hashCode >> 16) & 0xff));
        hc[1] = revertPos((int) ((hashCode >> 8) & 0xff) ^ (hashCode & 0xff));
        return hc;
    }

    public static String getRotorMap() {
        String[] keys = KEYS.split("");
        shuffleArray(keys);
        StringBuilder map = new StringBuilder(keys.length);
        for (String str : keys) {
            map.append(str);
        }

        return map.toString();
    }

    public static <T> void shuffleArray(T[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static String getReflectorMap() {
        char[] characters = new char[KEYS.length()];
        ArrayList<Character> charList = new ArrayList<Character>();
        for (char ch: KEYS.toCharArray()) {
            charList.add(ch);
        }

        Random rnd = new Random();
        for (int i= 0; i < KEYS.length(); i++) {
            if (characters[i] == '\u0000') {

                int index = rnd.nextInt(charList.size());
                while (index == 0) {
                    index = rnd.nextInt(charList.size());
                }
                Character c = charList.get(index);
                characters[i] = c;
                characters[getPos(c)] = revertPos(i);
                charList.remove(index);
                charList.remove(0);
            }
        }
        return String.valueOf(characters);
    }

}
