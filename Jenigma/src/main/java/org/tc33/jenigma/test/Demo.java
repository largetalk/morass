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
    
    public static void cookieDemo() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 8; j < 20; j++) {
            CookieEncrypt.encrypt(getRandomString(j), "5AOCoWvyViND6hMi");
            }
        }
        System.out.println(CookieEncrypt.times);
        System.out.println(CookieEncrypt.plainLength);
        System.out.println(CookieEncrypt.ciperLength);
        System.out.println((float) CookieEncrypt.ciperLength/CookieEncrypt.plainLength);
        
    }
    
    public static void benchmarkCookie() {
        String[] plainText = {
            "dzG8hfOsz7wNmfGxq",
            "NDxJHmWLWYxPlQ4qU",
            "1y0AlnOjhqAAzhoZjN",
            "WEeW7VWDq1mS8xfXOZ",
            "odTYD7ulutROQH9EO9GJpI",
            "D02znoiDTf6ulQxN0sxFki",
            "HGXAGfz9nQgzxh3grs9OdxYT",
            "jcs3KObMOG3B5GXjLtPhqGJE",
            "NWgPV7pSqTsK5iEVVWRxtU2OItV",
            "ZC4QsjqcyzVdr66YK40VJRiorpp",
            "vXzR0aZhyvKnwXUVfyCbTuSwPLLhj",
            "Cmbg8xINPhHoNhXQX99bVQRvoeaoH",
            "CjkEW2LKw2pEnJey77Y5cHIPDFi0GMD",
            "kQilTiKnKr6vbVAzVotbleY6nOzqRym",
            "8snkh0CW4wXkQnrjkyamyAabD5rbJIz"
        };
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            for (String str : plainText) {
                String tmp = CookieEncrypt.encrypt(str, "5AOCoWvyViND6hMi");
                CookieEncrypt.Dencrypt(tmp, "5AOCoWvyViND6hMi");
            }
        }
        long endTime = System.currentTimeMillis();
        String result = String.format("execute %s times and used time: %s ms", CookieEncrypt.times, endTime-startTime);

        System.out.println(result);
    }
    
    public static String getRandomString(int length) { //length表示生成字符串的长度
    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
    Random random = new Random();   
    StringBuffer sb = new StringBuffer();   
    for (int i = 0; i < length; i++) {   
        int number = random.nextInt(base.length());   
        sb.append(base.charAt(number));   
    }   
    return sb.toString();   
 }  

    public static void cookieCustomDemo() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 8; j < 25; j++) {
                CookieEncrypt.customEncrypt(getRandomString(j), "5AOCoWvy");
            }
        }
        System.out.println(CookieEncrypt.times);
        System.out.println(CookieEncrypt.plainLength);
        System.out.println(CookieEncrypt.ciperLength);
        System.out.println((float) CookieEncrypt.ciperLength / CookieEncrypt.plainLength);

    }
    
    public static void benchmarkCustomCookie() {
                String[] plainText = {
            "dzG8hfOsz7wNmfGxq",
            "NDxJHmWLWYxPlQ4qU",
            "1y0AlnOjhqAAzhoZjN",
            "WEeW7VWDq1mS8xfXOZ",
            "odTYD7ulutROQH9EO9GJpI",
            "D02znoiDTf6ulQxN0sxFki",
            "HGXAGfz9nQgzxh3grs9OdxYT",
            "jcs3KObMOG3B5GXjLtPhqGJE",
            "NWgPV7pSqTsK5iEVVWRxtU2OItV",
            "ZC4QsjqcyzVdr66YK40VJRiorpp",
            "vXzR0aZhyvKnwXUVfyCbTuSwPLLhj",
            "Cmbg8xINPhHoNhXQX99bVQRvoeaoH",
            "CjkEW2LKw2pEnJey77Y5cHIPDFi0GMD",
            "kQilTiKnKr6vbVAzVotbleY6nOzqRym",
            "8snkh0CW4wXkQnrjkyamyAabD5rbJIz"
        };
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            for (String str : plainText) {
                String tmp = CookieEncrypt.customEncrypt(str, "5AOCoWvy");
                CookieEncrypt.customDencrypt(tmp, "5AOCoWvy");
            }
        }
        long endTime = System.currentTimeMillis();
        String result = String.format("execute %s times and used time: %s ms", CookieEncrypt.times, endTime-startTime);

        System.out.println(result);
    }

    public static void test() {
        String plain = getRandomString(100);
        String cipher = CookieEncrypt.customEncrypt(plain, "5AOCoWvy");
        String decipher = CookieEncrypt.customDencrypt(cipher, "5AOCoWvy");
        System.out.println(plain);
        System.out.println(cipher);
        System.out.println(decipher);
        System.out.println(plain.equals(decipher));
        
        String wrong_decipher = CookieEncrypt.customDencrypt(cipher, "5AOCouvy");
        System.out.println(wrong_decipher);
        System.out.println(plain.equals(wrong_decipher));
        
        String wrong_cipher = cipher.replace('A', 'B').replace('t', 'Y');
        String the_wrong_decipher = CookieEncrypt.customDencrypt(wrong_cipher, "5AOCoWvy");
        System.out.println(the_wrong_decipher);
        System.out.println(plain.equals(the_wrong_decipher));
    }
    
    
    
    public static void main(String[] args) {
        //aesDemo();
        //urlBase64Demo();
        //cookieDemo();
        //benchmarkCookie();
        //cookieCustomDemo();
        benchmarkCustomCookie();
        test();

    }

}
