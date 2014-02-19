/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset.updater;

import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.dataset.DataSet;

public interface DataSetUpdater extends Initializable {

    public void update(DataSet dataSet);

    public void clear();

    public void close();
}
