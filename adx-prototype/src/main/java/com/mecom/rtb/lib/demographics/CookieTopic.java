/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.demographics;

import com.adsame.rtb.lib.cookiemanager.client.InputValue;
import com.adsame.rtb.lib.cookiemanager.client.OutputValue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieTopic extends OutputValue implements InputValue {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CookieTopic.class);

    public boolean lda = false;
    public byte topicMax = 100;
    public byte demographicsType = 1;
    public boolean time = false;
    public boolean importance = false;
    public boolean location = false;
    public boolean host = false;
    public boolean pv = false;
    public int cookieCount = 1;
    public long adsameCookie;

    public CookieTopic() {
    }

    public CookieTopic(long adsameCookie) {
        this.adsameCookie = adsameCookie;
    }

    @Override
    public CookieTopic readObject(DataInputStream dataInputStream) {
        try {
            lda = dataInputStream.readBoolean();
            topicMax = dataInputStream.readByte();
            demographicsType = dataInputStream.readByte();
            time = dataInputStream.readBoolean();
            importance = dataInputStream.readBoolean();
            location = dataInputStream.readBoolean();
            host = dataInputStream.readBoolean();
            pv = dataInputStream.readBoolean();
            cookieCount = dataInputStream.readInt();
            adsameCookie = dataInputStream.readLong();
            return this;
        } catch (IOException ex) {
            LOGGER.error("input stream write error", ex);
            return null;
        }
    }

    @Override
    public void writeObject(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeBoolean(lda);
            dataOutputStream.writeByte(topicMax);
            dataOutputStream.writeByte(demographicsType);
            dataOutputStream.writeBoolean(time);
            dataOutputStream.writeBoolean(importance);
            dataOutputStream.writeBoolean(location);
            dataOutputStream.writeBoolean(host);
            dataOutputStream.writeBoolean(pv);
            dataOutputStream.writeInt(cookieCount);
            dataOutputStream.writeLong(adsameCookie);
        } catch (IOException ex) {
            LOGGER.error("output stream write error", ex);
        }
    }

    @Override
    public String toString() {
        String string = "CookieTopic = { ";
        string += "lda = " + lda + ", ";
        string += "topicMax = " + topicMax + ", ";
        string += "demographicsType = " + demographicsType + ", ";
        string += "time = " + time + ", ";
        string += "importance = " + importance + ", ";
        string += "location = " + location + ", ";
        string += "host = " + host + ", ";
        string += "pv = " + pv + ", ";
        string += "cookieCount = " + cookieCount + ", ";
        string += "adsameCookie = " + adsameCookie + " }";
        return string;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else if (this == object) {
            return true;
        } else if (getClass() != object.getClass()) {
            return false;
        }

        CookieTopic rhs = (CookieTopic) object;
        return new EqualsBuilder().
                append(lda, rhs.lda).
                append(topicMax, rhs.topicMax).
                append(demographicsType, rhs.demographicsType).
                append(time, rhs.time).
                append(importance, rhs.importance).
                append(location, rhs.location).
                append(host, rhs.host).
                append(pv, rhs.pv).
                append(cookieCount, rhs.cookieCount).
                append(adsameCookie, rhs.adsameCookie).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 97).
                append(lda).
                append(topicMax).
                append(demographicsType).
                append(time).
                append(importance).
                append(location).
                append(host).
                append(pv).
                append(cookieCount).
                append(adsameCookie).
                toHashCode();
    }
}
