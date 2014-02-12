/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CookieMixerTest {

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
    public void testCorrectness1() {
        String cookieID = "316abe741defb1";
        String dspid = "mediav";
        String encrypted = CookieMixer.mix(cookieID, dspid);
        String decrypted = CookieMixer.demix(encrypted);

        Assert.assertEquals(decrypted, cookieID);
    }

    @Test
    public void testCorrectness2() {
        int testCount = 1000;
        for (int i = 0; i < testCount; i++) {
            String cookieID = EncryptUtils.getRandString(1000);
            String dspid = EncryptUtils.getRandString(20);

            String encrypted = CookieMixer.mix(cookieID, dspid);
            String decrypted = CookieMixer.demix(encrypted);

            Assert.assertEquals(decrypted, cookieID);
        }
    }
}
