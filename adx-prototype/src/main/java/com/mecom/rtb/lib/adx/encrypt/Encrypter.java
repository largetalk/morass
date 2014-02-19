/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public final class Encrypter {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 16;
    private static final String CHARSET_NAME = "UTF-8";

    public static String encrypt(String content, String token) {
        try {
            byte[] key = decodeKey(token);
            byte[] plaintext = content.getBytes(CHARSET_NAME);
            byte[] encryptedBytes = crypt(plaintext, key, Cipher.ENCRYPT_MODE);
            return Base64.encodeBase64URLSafeString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encrypted, String token) {
        try {
            byte[] key = decodeKey(token);
            byte[] encryptedBytes = Base64.decodeBase64(encrypted);
            byte[] plaintext = crypt(encryptedBytes, key, Cipher.DECRYPT_MODE);
            return new String(plaintext, CHARSET_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] decodeKey(String token) throws
            NullPointerException, IllegalArgumentException, DecoderException {
        if (token == null) {
            throw new NullPointerException("token is null");
        }

        byte[] key = Hex.decodeHex(token.toCharArray());
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("key length is not equals " + KEY_LENGTH);
        }
        return key;
    }

    private static byte[] crypt(byte[] content, byte[] key, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        cipher.init(mode, skeySpec);
        return cipher.doFinal(content);
    }
}
