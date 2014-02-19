/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZlibUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZlibUtility.class);

    public static final int BUFFER_LEN = 1024;

    public static byte[] compress(byte[] data) {
        byte[] output = null;

        Deflater compresser = new Deflater();

        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream compressOutput = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[BUFFER_LEN];
            while (!compresser.finished()) {
                int len = compresser.deflate(buf);
                compressOutput.write(buf, 0, len);
            }
            output = compressOutput.toByteArray();
        } catch (Exception ex) {
            LOGGER.error("compress data exception happen!", ex);
        }

        compresser.end();
        return output;
    }

    public static byte[] decompress(byte[] data) {
        byte[] output = null;

        Inflater decompresser = new Inflater();
        decompresser.setInput(data);

        ByteArrayOutputStream decompressOutput = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[BUFFER_LEN];
            while (!decompresser.finished()) {
                int len = decompresser.inflate(buf);
                decompressOutput.write(buf, 0, len);
            }
            output = decompressOutput.toByteArray();
        } catch (Exception ex) {
            LOGGER.error("decompress data exception happen!", ex);
        }

        decompresser.end();
        return output;
    }
}
