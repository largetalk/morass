/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.algorithm.topic.Document;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DocumentSerializer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentSerializer.class);

    public static Document readObject(DataInputStream dataInputStream) {
        Document document = null;

        try {
            byte length = dataInputStream.readByte();

            short ids[] = new short[length];
            float weights[] = new float[length];
            for (int i = 0; i < length; i++) {
                ids[i] = dataInputStream.readShort();
                weights[i] = dataInputStream.readFloat();
            }

            document = new Document(ids, weights);
        } catch (IOException ex) {
            LOGGER.error("input stream read error", ex);
        }

        return document;
    }

    public static void writeObject(Document document,
            DataOutputStream dataOutputStream) {

        try {
            byte length = (byte) document.topicIDs.length;
            dataOutputStream.writeByte(length);

            for (int i = 0; i < length; i++) {
                dataOutputStream.writeShort(document.topicIDs[i]);
                dataOutputStream.writeFloat(document.topicValues[i]);
            }
        } catch (IOException ex) {
            LOGGER.error("output stream write error", ex);
        }
    }
}
