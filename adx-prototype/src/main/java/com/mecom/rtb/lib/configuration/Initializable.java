/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.configuration;

import java.io.IOException;
import java.io.OutputStream;

public interface Initializable {

    public void initialize(Configuration configuration);

    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException;
}
