/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DemographicsSerializer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DemographicsSerializer.class);

    public static Demographics readObject(DataInputStream dataInputStream) {
        Demographics demographics = null;

        try {
            byte gender = dataInputStream.readByte();
            byte age = dataInputStream.readByte();
            byte income = dataInputStream.readByte();
            byte educate = dataInputStream.readByte();
            byte career = dataInputStream.readByte();
            demographics = new Demographics(gender, age,
                    income, educate, career);
        } catch (IOException ex) {
            LOGGER.error("input stream read error", ex);
        }

        return demographics;
    }

    public static void writeObject(Demographics demographics,
            DataOutputStream dataOutputStream) {

        try {
            dataOutputStream.writeByte(demographics.gender);
            dataOutputStream.writeByte(demographics.age);
            dataOutputStream.writeByte(demographics.income);
            dataOutputStream.writeByte(demographics.educate);
            dataOutputStream.writeByte(demographics.career);
        } catch (IOException ex) {
            LOGGER.error("output stream write error", ex);
        }
    }
}
