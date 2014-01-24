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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Encrypter {

    public static String encrypt(String content, String passwd) {
        if (passwd.length() != 16) return null;
        try {
            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
            
            SecretKeySpec key = new SecretKeySpec(passwd.getBytes(), "AES");
            aesECB.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = aesECB.doFinal(content.getBytes("utf-8"));
            //return new BASE64Encoder().encode(result);
            BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
            return b64.encode(encryptedBytes);
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
