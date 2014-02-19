/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.userid;

import com.adsame.rtb.lib.cookiemanager.client.InputValue;
import com.adsame.rtb.lib.cookiemanager.client.OutputValue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondUserID extends OutputValue implements InputValue {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SecondUserID.class);

    private String id;
    private String version;
    private String partyID;

    public SecondUserID() {
    }

    public SecondUserID(String id, String version, String partyID) {
        this.id = id;
        this.version = version;
        this.partyID = partyID;
    }

    @Override
    public SecondUserID readObject(DataInputStream dataInputStream) {
        try {
            id = dataInputStream.readUTF();
            version = dataInputStream.readUTF();
            partyID = dataInputStream.readUTF();
            return this;
        } catch (IOException ex) {
            LOGGER.error("input stream read error", ex);
        }
        return null;
    }

    @Override
    public void writeObject(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeUTF(id);
            dataOutputStream.writeUTF(version);
            dataOutputStream.writeUTF(partyID);
        } catch (IOException ex) {
            LOGGER.error("output stream write error", ex);
        }
    }

    public String getID() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getPartyID() {
        return partyID;
    }

    @Override
    public String toString() {
        String string = "SecondUserID = { ";
        string += "id = \"" + id + "\" , ";
        string += "ver = \"" + version + "\", ";
        string += "pid = \"" + partyID + "\" }";
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

        SecondUserID rhs = (SecondUserID) object;
        return new EqualsBuilder().
                append(id, rhs.id).
                append(version, rhs.version).
                append(partyID, rhs.partyID).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 97).
                append(id).
                append(version).
                append(partyID).
                toHashCode();
    }
}
