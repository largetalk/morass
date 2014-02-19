/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

public enum GeneralFieldEnum {
    SIZE("size"),
    URL("url"),
    IP("ip"),
    BROWSER("browser"),
    OS("os"),
    ATTRIBUTES("attributes"),
    CATEGORIES("categories"),
    LANDINGPAGEURL("landingPageUrl"),
    VIDEO("video"),
    EXTENSION("extension"),
    VOCATION("vocation"),
    CLICKURLS("clickURLs"),
    CREATIVETYPES("creativeTypes"),

    RETARGETING("retargeting"),
    FREQUENCY("frequency"),

    //this only for GlobalSolutionID
    ENTERFC("enterFC"),
    ENTERFCSIZE("enterFCSize"),

    DEMOGRAPHICS("demograhics"),

    OUTBID("outbid");

    private final String value;

    private GeneralFieldEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
