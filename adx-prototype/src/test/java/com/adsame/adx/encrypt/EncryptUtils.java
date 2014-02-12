/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.adx.encrypt;

import java.util.Random;

public class EncryptUtils {
    
        public static String getRandString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789"
                + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder randStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            randStringBuilder.append(base.charAt(number));
        }
        return randStringBuilder.toString();
    }
}
