/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.common;

public class Transport {

    private byte token[];
    private byte data[];

    public Transport() {
    }

    public Transport(byte token[], byte data[]) {
        this.token = token;
        this.data = data;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte token[]) {
        this.token = token;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte data[]) {
        this.data = data;
    }
}
