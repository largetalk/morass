package org.tc33.jenigma.test;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.*;

public class Demo {

    public static void aesDemo() {
        String plaintext = "win price is 20 yuan.";
        String password = "5AOCoWvyViND6hMi";
        String token = Hex.toHexString(password.getBytes());
        String cipher_text = Encrypter.encrypt(plaintext, password);
        assert (cipher_text != null);
        String decrypt_text = Decrypter.decrypt(cipher_text, token);

        System.out.println("normal test:");
        System.out.println(plaintext);
        System.out.println(token);
        System.out.println(decrypt_text);
        System.out.println();

        System.out.println("wrong password:");
        String wrong_passwd = "iMh6DNiVyvWoCOA5";
        String wrong_token = Hex.toHexString(wrong_passwd.getBytes());
        String wrong_decrypt_text = Decrypter.decrypt(cipher_text, wrong_token);
        assert (wrong_decrypt_text == null);
        System.out.println(plaintext);
        System.out.println(wrong_token);
        System.out.println(wrong_decrypt_text);
        System.out.println();

        System.out.println("wrong cipher text:");
        StringBuilder sb = new StringBuilder(cipher_text);
        Random rnd = new Random();
        for (int i = 0; i < 3; i++) {
            int index = rnd.nextInt(cipher_text.length());
            sb.setCharAt(index, sb.charAt(i));
        }
        String wrong_cipher_text = sb.toString();
        String decrypt_text_3 = Decrypter.decrypt(wrong_decrypt_text, token);
        assert (decrypt_text_3 == null);
        System.out.println(plaintext);
        System.out.println(token);
        System.out.println(decrypt_text_3);
        System.out.println("===================================");

    }

    public static void urlBase64Demo() {
        String pageUrl = "http://site.com?a=1&b=2";
        String asid = "asid1234abc";
        String siteId = "sohu.com";
        String ip = "211.32.15.23";
        String ua = "Mozilla/5.0 (X11; Linux i686)";
        String t = String.valueOf(System.currentTimeMillis());

        String b64pageUrl = URLEncoder.encode(pageUrl);
        String b64asid = URLEncoder.encode(asid);
        String b64siteId = URLEncoder.encode(siteId);
        String b64ip = URLEncoder.encode(ip);
        String b64ua = URLEncoder.encode(ua);
        String b64t = URLEncoder.encode(t);

/*
        String b64pageUrl = B64Encrypter.encrypt(pageUrl);
        String b64asid = B64Encrypter.encrypt(asid);
        String b64siteId = B64Encrypter.encrypt(siteId);
        String b64ip = B64Encrypter.encrypt(ip);
        String b64ua = B64Encrypter.encrypt(ua);
        String b64t = B64Encrypter.encrypt(t);*/

        String parameters = String.format("pageurl=%s&asid=%s&siteId=%s&ip=%s&ua=%s&t=%s",
                b64pageUrl, b64asid, b64siteId, b64ip, b64ua, b64t);
        System.out.println(parameters + "  " + parameters.length());

        String url = String.format("http://adx.adsame.com/?p=%s", B64Encrypter.encrypt(parameters));
        System.out.println(url + "  " + url.length());

        try {
            URL inUrl = new URL(url);
            String query = inUrl.getQuery();
            //System.out.println(query);
            Map<String, String> outParamsMap = analysisQuery(query);
            String outParams = B64Encrypter.decrypt(outParamsMap.get("p"));

            System.out.println(parameters.equals(outParams));
            Map<String, String> paramsMap = analysisQuery(outParams);
            Iterator it = paramsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                System.out.print(entry.getKey());
                System.out.print("----");
                System.out.println(entry.getValue());
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String, String> analysisQuery(String query) {
        String[] queryStringSplit = query.split("&");
        Map<String, String> queryStringMap
                = new HashMap<String, String>(queryStringSplit.length);
        String[] queryStringParam;
        for (String qs : queryStringSplit) {
            queryStringParam = qs.split("=");
            try {
            queryStringMap.put(queryStringParam[0], URLDecoder.decode(queryStringParam[1], "utf-8"));
            } catch (UnsupportedEncodingException ex) {
                queryStringMap.put(queryStringParam[0], queryStringParam[1]);
            }
        }

        return queryStringMap;
    }

    public static void main(String[] args) {
        aesDemo();
        urlBase64Demo();
    }

}
