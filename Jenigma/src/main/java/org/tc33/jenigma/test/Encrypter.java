package org.tc33.jenigma.test;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;


public class Encrypter {

    public static String encrypt(String content, String token) {
        byte[] passwd = Hex.toBytes(token);
        if (passwd.length != 16) return null;
        try {
            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
            
            SecretKeySpec key = new SecretKeySpec(passwd, "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = aesECB.doFinal(content.getBytes("utf-8"));
            //BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
            //return b64.encode(encryptedBytes);
            return Base64.encodeBase64URLSafeString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String decrypt(String encrypted, String token) {
        byte[] key = Hex.toBytes(token);
        if ((key == null) || (key.length != 16)) {
            return null;
        }
        try {
            
            //BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
            //byte[] encryptedBytes = b64.decode(encrypted);
            byte[] encryptedBytes = Base64.decodeBase64(encrypted);

            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] original = cipher.doFinal(encryptedBytes);
            return new String(original, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args) {
        float price = 500;
        String token = "35414f436f57767956694e4436684d69";
        String encrypted = encrypt(String.valueOf(price), token);
        System.out.println(encrypted);
        System.out.println(decrypt(encrypted, token));
    }

}
