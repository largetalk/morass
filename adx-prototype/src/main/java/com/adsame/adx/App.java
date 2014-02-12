package com.adsame.adx;

import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class App {

    public static void testBase64Len() {
        long total_len = 0;//所有测试字符串总长度
        long total_after = 0;//所有测试字符串base64编码后长度
        int count = 0;//次数
        double times = 0.0;//所有 编码后长度/编码前长度 之和
        for (int len = 100; len < 3000; len += 10, count++) {
            total_len += len;
            String original = getRandString(len);
            String after = Base64.encodeBase64URLSafeString(original.getBytes());
            int after_len = after.length();
            times += (double) after_len / len;
            total_after += after_len;
            //System.out.println((double) after_len / len);
        }
        
        System.out.println(times/count);
        System.out.println( (double) total_after / total_len);
    }

    public static void main(String[] args) {
        testBase64Len();
        testHex();
    }
    public static void testHex() {
        try{
        String randomStr = getRandString(16);
        String hexStr = Hex.encodeHexString(randomStr.getBytes());
        String revertStr = new String(Hex.decodeHex(hexStr.toCharArray()));
        System.out.println(randomStr);
        System.out.println(hexStr);
        System.out.println(revertStr);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getRandString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+:|?.,[]{}";
        Random random = new Random();
        StringBuilder randStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            randStringBuilder.append(base.charAt(number));
        }
        return randStringBuilder.toString();
    }
}
