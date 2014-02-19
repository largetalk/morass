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

public class UserIDEntry extends OutputValue implements InputValue {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserIDEntry.class);

    private String firstID;
    private SecondUserID secondUserID;

    public UserIDEntry() {
        secondUserID = new SecondUserID();
    }

    public UserIDEntry(String firstID, String secondID, String secondVersion,
            String secondPartyID) {
        this(firstID, new SecondUserID(secondID, secondVersion, secondPartyID));
    }

    public UserIDEntry(String firstID, SecondUserID secondUserID) {
        this.firstID = firstID;
        this.secondUserID = secondUserID;
    }

    @Override
    public UserIDEntry readObject(DataInputStream dataInputStream) {
        UserIDEntry userIDEntry = null;
        try {
            firstID = dataInputStream.readUTF();
            secondUserID.readObject(dataInputStream);
            return this;
        } catch (IOException ex) {
            LOGGER.error("input stream read error", ex);
        }
        return userIDEntry;
    }

    @Override
    public void writeObject(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeUTF(firstID);
            secondUserID.writeObject(dataOutputStream);
        } catch (IOException ex) {
            LOGGER.error("input stream write error", ex);
        }
    }

    public boolean existsValue(byte errorCode) {
        return false;
    }

    public String getFirstID() {
        return firstID;
    }

    public SecondUserID getSecondUserID() {
        return secondUserID;
    }

    @Override
    public String toString() {
        String string = "UserIDEntry = { ";
        string += "fid = \"" + firstID + "\", ";
        string += "sid = \"" + secondUserID.getID() + "\" , ";
        string += "sver = \"" + secondUserID.getVersion() + "\" , ";
        string += "spid = \"" + secondUserID.getPartyID() + "\" }";
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

        UserIDEntry rhs = (UserIDEntry) object;
        return new EqualsBuilder().
                append(firstID, rhs.firstID).
                append(secondUserID, rhs.secondUserID).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 23).
                append(firstID).
                append(secondUserID).
                toHashCode();
    }
}
