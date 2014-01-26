/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tc33.jenigma.test;

import com.google.common.io.BaseEncoding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author adsame
 */
public class CookieEncrypt {
    public static int times = 0;
    public static long plainLength = 0;
    public static long ciperLength = 0;

    
    public static String Dencrypt(String crypted, String password) {
        return Decrypter.decrypt(crypted, Hex.toHexString(password.getBytes()));
    }
    public static String encrypt(String content, String password) {
        times += 1;
        plainLength += content.length();
        String cipher_text = Encrypter.encrypt(content, password);
        ciperLength += cipher_text.length();
        return cipher_text;
        
//        
//        if (password.length() != 16) return null;
//        try {
//            Cipher aesECB = Cipher.getInstance("AES/ECB/PKCS5Padding");
//
//            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
//            aesECB.init(Cipher.ENCRYPT_MODE, key);
//            byte[] encryptedBytes = aesECB.doFinal(content.getBytes("utf-8"));
//
//            byte[] compressedEncrypted = compress(encryptedBytes);
//
//
//            BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
//            String cipher_text = b64.encode(compressedEncrypted);
//            
//            System.out.println(cipher_text.length());
//            ciperLength += cipher_text.length();
//            return cipher_text;
//            //return b64.encode(encryptedBytes);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return null;


    }
    
    public static String customEncrypt(String content, String password) {
        times += 1;
        plainLength += content.length();
        if (password.length() != 8) {
            return null;
        }
        int blockSize = 4;

        byte[] dataBytes = content.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }

        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        
        for (int i=0; i < plaintextLength; i++) {
            if (i%2==0) {
                plaintext[i] ^= i;
            } else {
                plaintext[i] ^= (plaintextLength - i);
            }
            plaintext[i] = bswap(plaintext[i]);
        }
        
        //System.out.println(Hex.toHexString(plaintext));
     
        int salt = 385751695;
        byte[] key1 =  new byte[4];
        System.arraycopy(password.getBytes(), 0, key1, 0, 4);
        key1 = int2byte(byte2int(key1) ^ salt++);
        byte[] key2 = new byte[4];
        System.arraycopy(password.getBytes(), 4, key2, 0, 4);
        key2 = int2byte(byte2int(key2) ^ salt++);
        
        key1[0] = (byte)(key1[0] ^ key2[0] ^ key2[1]);
        key1[1] = (byte)(key1[1] ^ key2[0] ^ key2[2]);
        key1[2] = (byte)(key1[2] ^ key2[2] ^ key2[3]);
        key1[3] = (byte)(key1[3] ^ key2[1] ^ key2[3]);
        
        //System.out.println(Hex.toHexString(key1));
        //System.out.println(Hex.toHexString(key2));
        
        int k = 0;
        byte[] output = new byte[plaintextLength];
        while (k < plaintext.length/4) {
            if (k%2==0) {
                byte[] tmp1 = new byte[4];
                for (int j=0;j<4;j++)
                {
                    tmp1[j] = (byte) (key1[j] ^ plaintext[ 4*k +j ]);
                }
                System.arraycopy(tmp1, 0, output, 4*k, 4);
            } else {
                byte[] tmp2 = new byte[4];
                for (int j=0;j<4;j++)
                {
                    tmp2[j] = (byte) (key1[j] ^ key2[j] ^ plaintext[ 4*k +j]);
                }
                System.arraycopy(tmp2, 0, output, 4*k, 4);
            }
            k++;
        }
        //System.out.println(Hex.toHexString(output));
        
        BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
        String cipher_text = b64.encode(output);
 
        ciperLength += cipher_text.length();
        return cipher_text;
        
    }
    
    public static String customDencrypt(String encrypted, String password) {

        if (password.length() != 8) {
            return null;
        }
        BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
        byte[] encryptedBytes = b64.decode(encrypted);
        
        //System.out.println(Hex.toHexString(encryptedBytes));

        int encryptedLength = encryptedBytes.length;
                
        int blockSize = 4;
        if (encryptedLength % blockSize != 0) {
            return null;
        }
        
        int salt = 385751695;
        byte[] key1 =  new byte[4];
        System.arraycopy(password.getBytes(), 0, key1, 0, 4);
        key1 = int2byte(byte2int(key1) ^ salt++);
        byte[] key2 = new byte[4];
        System.arraycopy(password.getBytes(), 4, key2, 0, 4);
        key2 = int2byte(byte2int(key2) ^ salt++);
 
        key1[0] = (byte)(key1[0] ^ key2[0] ^ key2[1]);
        key1[1] = (byte)(key1[1] ^ key2[0] ^ key2[2]);
        key1[2] = (byte)(key1[2] ^ key2[2] ^ key2[3]);
        key1[3] = (byte)(key1[3] ^ key2[1] ^ key2[3]);
        
        //System.out.println(Hex.toHexString(key1));
        //System.out.println(Hex.toHexString(key2));
        
        int k = 0;
        byte[] output = new byte[encryptedLength];
        while (k < encryptedLength/4) {
            if (k%2==0) {
                byte[] tmp1 = new byte[4];
                for (int j=0;j<4;j++)
                {
                    tmp1[j] = (byte) (key1[j] ^ encryptedBytes[ 4*k +j ]);
                }
                System.arraycopy(tmp1, 0, output, 4*k, 4);
            } else {
                byte[] tmp2 = new byte[4];
                for (int j=0;j<4;j++)
                {
                    tmp2[j] = (byte) (key1[j] ^ key2[j] ^ encryptedBytes[ 4*k +j]);
                }
                System.arraycopy(tmp2, 0, output, 4*k, 4);
            }
            k++;
        }

     
        for (int i=0; i < encryptedLength; i++) {
            output[i] = bswap(output[i]);

            if (i%2==0) {
                output[i] ^= i;
            } else {
                output[i] ^= (encryptedLength - i);
            }

        }

        return new String(output);
        
    }
    
     public static byte bswap(byte a)
    {
        byte b = 0;
        for(int i = 0; i < 8; ++i)
            b |= ((a & (1 << i)) == 0 ? 0 : 1) << (7-i);
        return b;
    }

    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位 
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位 
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位 
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。 
        return targets;
    }

    public static int byte2int(byte[] res) {
// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000 

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或 
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public static byte[] compress(byte[] data) {
        byte[] output = null;

        Deflater compresser = new Deflater();
        compresser.reset();
        compresser.setInput(data);
        compresser.finish();

        ByteArrayOutputStream compressOutput = new ByteArrayOutputStream(data.length);

        try {
            byte[] buf = new byte[4096];
            while (!compresser.finished()) {
                int len = compresser.deflate(buf);
                compressOutput.write(buf, 0, len);
            }
            output = compressOutput.toByteArray();
        } catch (Exception ex) {
            output = null;
        } finally {
            try {
                compressOutput.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
            
        compresser.end();
        return output;

    }

}
