/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncrypterTest {

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
        double price = 500.0;
        long curTime = System.currentTimeMillis();
        String plain = String.format("%s_%s", price, curTime);
        String token = "35414f436f57767956694e4436684d69";
        String encrypted = Encrypter.encrypt(plain, token);
        String decrypted = Encrypter.decrypt(encrypted, token);

        Assert.assertEquals(decrypted, plain);
    }

    @Test
    public void testCorrectness2() {
        int testCount = 1000;
        for (int i = 0; i < testCount; i++) {
            String plain = EncryptUtils.getRandString(1000);
            String passwd = EncryptUtils.getRandString(16);
            String token = Hex.encodeHexString(passwd.getBytes());

            String encrypted = Encrypter.encrypt(plain, token);
            String decrypted = Encrypter.decrypt(encrypted, token);

            Assert.assertEquals(decrypted, plain);
        }
    }
}
