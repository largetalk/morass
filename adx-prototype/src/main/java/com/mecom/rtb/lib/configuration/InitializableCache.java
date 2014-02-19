/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.configuration;

import java.util.ArrayList;
import java.util.HashMap;

public class InitializableCache<T extends Initializable> {

    private InitializableLoader<T> loader;

    private HashMap<String, T> map;

    public InitializableCache(InitializableLoader<T> loader) {
        this.loader = loader;

        map = new HashMap<String, T>();
    }

    public T get(String name) {
        return map.get(name);
    }

    public boolean refresh(String name) {
        T currentInstance = map.get(name);
        T instance = loader.load(name, currentInstance);
        if (instance != null) {
            map.put(name, instance);
            return true;
        } else {
            map.remove(name);
            return false;
        }
    }

    public boolean remove(String name) {
        if (map.containsKey(name)) {
            map.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> list() {
        return new ArrayList<String>(map.keySet());
    }
}
