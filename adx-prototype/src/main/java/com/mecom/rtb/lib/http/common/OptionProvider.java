/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.http.common;

public interface OptionProvider {

    public Object getOption(String key);

    public void setOption(String key, Object value);
}
