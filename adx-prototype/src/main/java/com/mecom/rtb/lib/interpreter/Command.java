/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.interpreter;

public interface Command {

    public String getName();

    public boolean execute(String parameters[]);
}
