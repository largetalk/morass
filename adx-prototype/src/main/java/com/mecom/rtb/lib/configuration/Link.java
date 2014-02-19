/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.configuration;

import java.io.File;

public final class Link {

    private String parent;
    private String path;

    public Link(String path) {
        parent = new File(".").getParent();
        this.path = path;
    }

    public Link(String parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    public String getParent() {
        return parent;
    }

    public String getPath() {
        return path;
    }

    public String getFullPath() {
        if (new File(path).isAbsolute()) {
            return path;
        } else {
            return new File(parent, path).toString();
        }
    }
}
