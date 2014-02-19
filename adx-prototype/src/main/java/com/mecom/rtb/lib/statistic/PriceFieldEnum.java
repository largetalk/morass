/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public enum PriceFieldEnum {
    PRICE("price");

    private final String value;

    private PriceFieldEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
