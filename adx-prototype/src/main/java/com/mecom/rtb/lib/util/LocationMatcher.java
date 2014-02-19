/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

public class LocationMatcher {

    private DictionaryTree tree;

    public LocationMatcher(String locationPatternArray[]) {
        tree = new DictionaryTree();
        for (int i = 0; i < locationPatternArray.length; i++) {
            String locationPattern = locationPatternArray[i];
            int index = locationPattern.indexOf('*');
            if (index != -1) {
                locationPattern = locationPattern.substring(0, index);
            }
            tree.insert(locationPattern);
        }
    }

    public boolean matches(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        } else {
            return tree.containsPrefixOfWord(location);
        }
    }
}
