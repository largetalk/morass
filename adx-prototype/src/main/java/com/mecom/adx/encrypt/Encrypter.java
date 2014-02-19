/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;


public final class Encrypter {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encrypt(String content, String token) {
        if (token == null) {
            return null;
        }

        try {
            byte[] passwd = Hex.decodeHex(token.toCharArray());
            if (passwd.length != 16) {
                return null;
            }

            Cipher aesECB = Cipher.getInstance(ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(passwd, "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = aesECB.doFinal(content.getBytes("utf-8"));
            return Base64.encodeBase64URLSafeString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encrypted, String token) {
        if (token == null) {
            return null;
        }

        try {
            byte[] key = Hex.decodeHex(token.toCharArray());
            if ((key == null) || (key.length != 16)) {
                return null;
            }
        
            byte[] encryptedBytes = Base64.decodeBase64(encrypted);

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] original = cipher.doFinal(encryptedBytes);
            return new String(original, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
