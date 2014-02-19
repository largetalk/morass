/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.HashMap;

public class DictionaryTree {

    private static final String DELIMITER = "$$";

    private HashMap<String, DictionaryTree> subTreeMap;
    private DictionaryTree parent;

    public DictionaryTree() {
        subTreeMap = new HashMap<String, DictionaryTree>();
        parent = null;
    }

    private DictionaryTree(DictionaryTree parent) {
        subTreeMap = new HashMap<String, DictionaryTree>();
        this.parent = parent;
    }

    private static final class Position {

        public int index;
        public DictionaryTree tree;

        public Position(int index, DictionaryTree tree) {
            this.index = index;
            this.tree = tree;
        }
    }

    public boolean contains(String string) {
        if (string == null) {
            return false;
        } else {
            char charArray[] = string.toCharArray();
            Position position = searchNode(this, charArray);
            int currentIndex = position.index;
            DictionaryTree currentTree = position.tree;
            if (currentIndex == charArray.length
                    && currentTree.subTreeMap.containsKey(DELIMITER)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean containsWordWithPrefix(String prefix) {
        if (prefix == null) {
            return false;
        } else {
            char charArray[] = prefix.toCharArray();
            Position position = searchNode(this, charArray);
            int currentIndex = position.index;
            if (currentIndex == charArray.length) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean containsPrefixOfWord(String word) {
        if (word == null) {
            return false;
        } else {
            char charArray[] = word.toCharArray();
            DictionaryTree currentTree = this;
            for (int i = 0; i < charArray.length; i++) {
                String key = String.valueOf(charArray[i]);
                if (currentTree.subTreeMap.containsKey(DELIMITER)) {
                    return true;
                } else if (currentTree.subTreeMap.containsKey(key)) {
                    currentTree = currentTree.subTreeMap.get(key);
                } else {
                    return false;
                }
            }
            if (currentTree.subTreeMap.containsKey(DELIMITER)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void insert(String string) {
        if (string != null) {
            char charArray[] = string.toCharArray();
            Position position = searchNode(this, charArray);
            int currentIndex = position.index;
            DictionaryTree currentTree = position.tree;
            for (int i = currentIndex; i < charArray.length; i++) {
                String key = String.valueOf(charArray[i]);
                DictionaryTree subTree = new DictionaryTree(currentTree);
                currentTree.subTreeMap.put(key, subTree);
                currentTree = subTree;
            }
            if (!currentTree.subTreeMap.containsKey(DELIMITER)) {
                currentTree.subTreeMap.put(DELIMITER, null);
            }
        }
    }

    public void remove(String string) {
        if (string != null) {
            char charArray[] = string.toCharArray();
            Position position = searchNode(this, charArray);
            int currentIndex = position.index;
            DictionaryTree currentTree = position.tree;
            if (currentIndex == charArray.length
                    && currentTree.subTreeMap.containsKey(DELIMITER)) {
                currentTree.subTreeMap.remove(DELIMITER);
                while (currentTree.parent != null
                        && currentTree.subTreeMap.isEmpty()) {
                    currentIndex -= 1;
                    currentTree = currentTree.parent;
                    String key = String.valueOf(charArray[currentIndex]);
                    currentTree.subTreeMap.remove(key);
                }
            }
        }
    }

    private static Position searchNode(DictionaryTree tree, char charArray[]) {
        int index;
        for (index = 0; index < charArray.length; index++) {
            String key = String.valueOf(charArray[index]);
            if (tree.subTreeMap.containsKey(key)) {
                tree = tree.subTreeMap.get(key);
            } else {
                break;
            }
        }
        return new Position(index, tree);
    }
}
