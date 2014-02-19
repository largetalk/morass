/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class LinkedProperties extends Properties {

    private LinkedHashSet<String> linkedKeySet;

    public LinkedProperties() {
        linkedKeySet = new LinkedHashSet<String>();
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        linkedKeySet.add((String) key);
        return super.put(key, value);
    }

    public synchronized Set<String> getLinkedKeySet() {
        return linkedKeySet;
    }
}
