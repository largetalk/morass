package org.tc33.jenigma.test;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

public class B64Encrypter {
    public static String encrypt(String content) {
        //if (Base64.isBase64(content)) return content;
        try {
            return Base64.encodeBase64URLSafeString(content.getBytes("utf-8"));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
        
//        try {
//        Base64 base64 = new Base64(true);//urlSafe = true
//        return new String(base64.encode(content.getBytes("utf-8")));
//        } catch (UnsupportedEncodingException ex) {
//            ex.printStackTrace();
//        }
//        return null;
    }
    
    public static String decrypt(String content) {
        if (!Base64.isBase64(content)) {
            return null;
        }
        return String.valueOf(Base64.decodeBase64(content));
    }
}
