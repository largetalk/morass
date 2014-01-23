/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tc33.jenigma;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author adsame
 */
public class AesDemo {

    public static String encrypt(String content, String passwd) {
        try {
            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(passwd.getBytes(), "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = aesECB.doFinal(content.getBytes());
            return new BASE64Encoder().encode(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
  
    public static String decrypt(String content, String passwd) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
            SecretKeySpec key = new SecretKeySpec(passwd.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = new BASE64Decoder().decodeBuffer(content);
            return new String(cipher.doFinal(result)); // 解密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private static String cbcEncrypt(String content, String passwd, String iv) {
         try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = content.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            
            SecretKeySpec keyspec = new SecretKeySpec(passwd.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new sun.misc.BASE64Encoder().encode(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String cbcDecrypt(String content, String passwd, String iv) {
          try {

            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(content);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keyspec = new SecretKeySpec(passwd.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
 
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String createPassword(int length ) {
        StringBuilder password = new StringBuilder(length);
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int num = random.nextInt(str.length());
            password.append(str.charAt(num));
        }
        return password.toString();
    }
    
    private static void ecbPKC5Pad16AES() {
        String plaintext = "this string include 1: UPCASE,2: number,3:中文 ";
        String password = createPassword(16); //this setting only support 16*8 key length
        String ciphertext = encrypt(plaintext, password);
        String decipheredtext = decrypt(ciphertext, password);
        System.out.println("Configuration: AES/ECB/PKCS5Padding ,Algorithm Name: AES ,key length: 16");
        System.out.println(plaintext);
        System.out.println(password);
        System.out.println(ciphertext);
        System.out.println(decipheredtext);
        System.out.println("=======================================\n");
    }
    
    private static void cbcNoPad16AES() {
        String plaintext = "this string include 1: UPCASE,2: number,3:中文 ";
        String password = createPassword(16); //this setting only support 16*8 key length
        String iv = createPassword(16);
        String ciphertext = cbcEncrypt(plaintext, password, iv);
        String decipheredtext = cbcDecrypt(ciphertext, password, iv);
        System.out.println("Configuration: AES/CBC/NoPadding ,Algorithm Name: AES ,key/iv length: 16");
        System.out.println(plaintext);
        System.out.println(password);
        System.out.println(iv);
        System.out.println(ciphertext);
        System.out.println(decipheredtext);
        System.out.println("=======================================\n");
    }
    

    public static void main(String[] args) {
        ecbPKC5Pad16AES();
        cbcNoPad16AES();
    }

}
