package org.tc33.jenigma.test;

import java.util.*;

public class Demo {
    public static void aesDemo() {
        String plaintext = "win price is 20 yuan.";
        String password = "5AOCoWvyViND6hMi";
        String token = Hex.toHexString(password.getBytes()); 
        String cipher_text = Encrypter.encrypt(plaintext, password);
        assert(cipher_text != null);
        String decrypt_text = Decrypter.decrypt(cipher_text, token);

        System.out.print("normal test:");
        System.out.println(plaintext);
        System.out.println(token);
        System.out.println(decrypt_text);
        
        System.out.println("wrong password:");
        String wrong_passwd = "iMh6DNiVyvWoCOA5";
        String wrong_token = Hex.toHexString(wrong_passwd.getBytes());
        String wrong_decrypt_text = Decrypter.decrypt(cipher_text, wrong_token);
        assert(wrong_decrypt_text == null);
        System.out.println(plaintext);
        System.out.println(wrong_token);
        System.out.println(wrong_decrypt_text);
        
        System.out.println("wrong cipher text:");
        StringBuilder sb = new StringBuilder(cipher_text);
        Random rnd = new Random();
        for (int i = 0; i < 3; i++) {
            int index = rnd.nextInt(cipher_text.length());
            sb.setCharAt(index, sb.charAt(i));
        }
        String wrong_cipher_text = sb.toString();
        String decrypt_text_3 = Decrypter.decrypt(wrong_decrypt_text, token);
        assert(decrypt_text_3 == null);
        System.out.println(plaintext);
        System.out.println(token);
        System.out.println(decrypt_text_3);
        
    }
    
    public static void main(String[] args) {
        aesDemo();
    }

}
