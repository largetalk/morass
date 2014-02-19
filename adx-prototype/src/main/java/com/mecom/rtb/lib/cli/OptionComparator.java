/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.cli;

import java.util.Comparator;
import java.util.HashMap;
import org.apache.commons.cli.Option;

public class OptionComparator implements Comparator<Option> {

    private final HashMap<String, Integer> orderMap;

    public OptionComparator(HashMap<String, Integer> orderMap) {
        this.orderMap = orderMap;
    }

    @Override
    public int compare(Option a, Option b) {
        return orderMap.get(a.getOpt()) - orderMap.get(b.getOpt());
    }
}
