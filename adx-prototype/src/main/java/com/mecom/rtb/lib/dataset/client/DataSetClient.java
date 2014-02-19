/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.client;

import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.dataset.DataSet;

public interface DataSetClient extends Initializable {

    public DataSet retrieveDataSet();

    public void close();
}
