/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

public class URLMatcher {

    private DictionaryTree tree;

    public URLMatcher(String urlPatternArray[]) {
        tree = new DictionaryTree();
        for (int i = 0; i < urlPatternArray.length; i++) {
            tree.insert(urlPatternArray[i]);
        }
    }

    public boolean matches(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        } else {
            return tree.containsPrefixOfWord(url);
        }
    }
}
