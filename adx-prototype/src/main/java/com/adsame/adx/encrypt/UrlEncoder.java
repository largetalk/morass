/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;

public class UrlEncoder {

    public static String DEFAULT_DES_KEY = "20010101";

    /**
     * DES Coder<br/>
     * secret key length:	56 bit, default:	56 bit<br/>
     * mode:	ECB/CBC/PCBC/CTR/CTS/CFB/CFB8 to CFB128/OFB/OBF8 to OFB128<br/>
     * padding:	Nopadding/PKCS5Padding/ISO10126Padding/
     */
    private static class DESCoder {

        /**
         * 密钥算法
         */
        private static final String KEY_ALGORITHM = "DES";

        private static final String DEFAULT_CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

        /**
         * 初始化密钥
         *
         * @return byte[] 密钥
         * @throws Exception
         */
        public static byte[] initSecretKey() throws Exception {
            //返回生成指定算法的秘密密钥的 KeyGenerator 对象
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            //初始化此密钥生成器，使其具有确定的密钥大小
            kg.init(56);
            //生成一个密钥
            SecretKey secretKey = kg.generateKey();
            return secretKey.getEncoded();
        }

        /**
         * 转换密钥
         *
         * @param key	二进制密钥
         * @return Key	密钥
         * @throws Exception
         */
        private static Key toKey(byte[] key) throws Exception {
            //实例化DES密钥规则
            DESKeySpec dks = new DESKeySpec(key);
            //实例化密钥工厂
            SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            //生成密钥
            SecretKey secretKey = skf.generateSecret(dks);
            return secretKey;
        }

        /**
         * 加密
         *
         * @param data	待加密数据
         * @param key	密钥
         * @return byte[]	加密数据
         * @throws Exception
         */
        public static byte[] encrypt(byte[] data, Key key) throws Exception {
            return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
        }

        /**
         * 加密
         *
         * @param data	待加密数据
         * @param key	二进制密钥
         * @return byte[]	加密数据
         * @throws Exception
         */
        public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
            return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
        }

        /**
         * 加密
         *
         * @param data	待加密数据
         * @param key	二进制密钥
         * @param cipherAlgorithm	加密算法/工作模式/填充方式
         * @return byte[]	加密数据
         * @throws Exception
         */
        public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
            //还原密钥
            Key k = toKey(key);
            return encrypt(data, k, cipherAlgorithm);
        }

        /**
         * 加密
         *
         * @param data	待加密数据
         * @param key	密钥
         * @param cipherAlgorithm	加密算法/工作模式/填充方式
         * @return byte[]	加密数据
         * @throws Exception
         */
        public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
            //实例化
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            //使用密钥初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //执行操作
            return cipher.doFinal(data);
        }

        /**
         * 解密
         *
         * @param data	待解密数据
         * @param key	二进制密钥
         * @return byte[]	解密数据
         * @throws Exception
         */
        public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
            return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
        }

        /**
         * 解密
         *
         * @param data	待解密数据
         * @param key	密钥
         * @return byte[]	解密数据
         * @throws Exception
         */
        public static byte[] decrypt(byte[] data, Key key) throws Exception {
            return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
        }

        /**
         * 解密
         *
         * @param data	待解密数据
         * @param key	二进制密钥
         * @param cipherAlgorithm	加密算法/工作模式/填充方式
         * @return byte[]	解密数据
         * @throws Exception
         */
        public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
            //还原密钥
            Key k = toKey(key);
            return decrypt(data, k, cipherAlgorithm);
        }

        /**
         * 解密
         *
         * @param data	待解密数据
         * @param key	密钥
         * @param cipherAlgorithm	加密算法/工作模式/填充方式
         * @return byte[]	解密数据
         * @throws Exception
         */
        public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
            //实例化
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, key);
            //执行操作
            return cipher.doFinal(data);
        }
    }

    public static String encodeUrl(Map<String, String> paramsMap) {
        return encodeUrl(paramsMap, DEFAULT_DES_KEY);
    }

    public static String encodeUrl(Map<String, String> paramsMap, String desKey) {
        byte[] encrypted = null;

        try {
            String query = generateQuery(paramsMap);
            encrypted = DESCoder.encrypt(query.getBytes(), desKey.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        String url = Base64.encodeBase64URLSafeString(encrypted);
        return url;
    }

    public static Map<String, String> decodeUrl(String encrypedUrl) {
        return decodeUrl(encrypedUrl, DEFAULT_DES_KEY);
    }

    public static Map<String, String> decodeUrl(String encrypedUrl, String desKey) {
        try {
            byte[] decoded = Base64.decodeBase64(encrypedUrl);
            byte[] decrypted = DESCoder.decrypt(decoded, desKey.getBytes());

            Map<String, String> paramsMap = analysisQuery(new String(decrypted));
            return paramsMap;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String generateQuery(Map<String, String> paramsMap) {
        StringBuilder builder = new StringBuilder();
        try {
            for (String key : paramsMap.keySet()) {
                String value = URLEncoder.encode(paramsMap.get(key), "utf-8");
                builder.append(key);
                builder.append("=");
                builder.append(value);
                builder.append("&");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Map<String, String> analysisQuery(String query) {
        String[] queryStringSplit = query.split("&");
        Map<String, String> queryStringMap
                = new HashMap<String, String>(queryStringSplit.length);
        String[] queryStringParam;
        for (String qs : queryStringSplit) {
            queryStringParam = qs.split("=");
            try {
                queryStringMap.put(queryStringParam[0],
                        URLDecoder.decode(queryStringParam[1], "utf-8"));
            } catch (UnsupportedEncodingException ex) {
                queryStringMap.put(queryStringParam[0], queryStringParam[1]);
            }
        }

        return queryStringMap;
    }

}
