/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.adx.encrypt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Test {

    public static void main(String args[]) throws UnsupportedEncodingException {
        String value = "http://adx.adsame.com   /?a=!&b=@#$%^&c=*()-_+= {}[]:;',<>\\|～`中文\r\n";
        String encoded = URLEncoder.encode(value, "UTF-8");
        System.out.println(encoded);
        
        String decoded = URLDecoder.decode(encoded, "UTF-8");
        System.out.println(decoded);
        
        String value2 = "http%3A%2F%2Fadx.adsame.com%2F%3Fa%3D!%26b%3D%40%23%24%25%5E%26c%3D*()-_%2B%3D%20%7B%7D%5B%5D%3A%3B'%2C%3C%3E%5C%7C%EF%BD%9E%60%E4%B8%AD%E6%96%87%0D%0A";
        System.out.println(URLDecoder.decode(value2, "UTF-8"));
    }
}
