/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.network.common;

public class Parameters {

    public static final int BLOCKING_HANDLER = 0;
    public static final int NONBLOCKING_HANDLER = 1;

    public static final String CLIENT_NO_DELAY = "tcpNoDelay";
    public static final String SERVER_NO_DELAY = "child.tcpNoDelay";

    public static final String CLIENT_KEEP_ALIVE = "keepAlive";
    public static final String SERVER_KEEP_ALIVE = "child.keepAlive";
}
