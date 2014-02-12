/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UrlEncoderTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCorrectness() {
        String pageUrl = "http://site.com?a=1&b=2";
        String asid = "asid1234abc";
        String siteId = "http://sohu.com";
        String ip = "211.32.15.23";
        String ua = "Mozilla/5.0 (X11; Linux i686)";
        String t = String.valueOf(System.currentTimeMillis());

        Map<String, String> params = new HashMap<String, String>();
        params.put("pageurl", pageUrl);
        params.put("asid", asid);
        params.put("siteid", siteId);
        params.put("ip", ip);
        params.put("ua", ua);
        params.put("t", t);

        String encodedUrl = UrlEncoder.encodeUrl(params);
        Map<String, String> decodedParams = UrlEncoder.decodeUrl(encodedUrl);

        Assert.assertEquals(params.size(), decodedParams.size());
        for (String key : params.keySet()) {
            Assert.assertTrue(decodedParams.containsKey(key));

            String value = params.get(key);
            String decode_value = decodedParams.get(key);
            Assert.assertEquals(value, decode_value);
        }
    }

    @Test
    public void testCorrectness2() {
        int testCount = 1000;
        for (int i = 0; i < testCount; i++) {
            Map<String, String> params = new HashMap<String, String>();
            for (int j = 0; j < 20; j++) {
                String name = EncryptUtils.getRandString(20);
                String value = EncryptUtils.getRandString(50);
                params.put(name, value);
            }

            String encodedUrl = UrlEncoder.encodeUrl(params);
            Map<String, String> decodedParams = UrlEncoder.decodeUrl(encodedUrl);

            Assert.assertEquals(params.size(), decodedParams.size());
            for (String key : params.keySet()) {
                Assert.assertTrue(decodedParams.containsKey(key));

                String value = params.get(key);
                String decode_value = decodedParams.get(key);
                Assert.assertEquals(value, decode_value);
            }
        }
    }
}
