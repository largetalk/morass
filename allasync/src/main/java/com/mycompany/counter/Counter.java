/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.counter;

import com.adsame.rtb.lib.configuration.Initializable;
import java.util.List;

public interface Counter extends Initializable {

    public void add(String pointID, List<String> cookies);

    public long getCount(String pointID);

    public boolean clear(String pointID);
}
