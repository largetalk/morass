/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.smartbid.common.dataset;

import com.adsame.rtb.lib.dataset.DataSet;
import com.adsame.rtb.lib.dataset.DataSetProcessor;
import com.adsame.rtb.lib.dataset.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDataSetProcessor extends DataSetProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CommonDataSetProcessor.class);

    @Override
    public final CommonDataSet process(DataSet dataSet) {
        return generateDataSet(dataSet);
    }

    protected CommonDataSet generateDataSet(DataSet dataSet) {
        int numTables = dataSet.getNumTables();
        Table tables[] = new Table[numTables];
        for (int i = 0; i < numTables; i++) {
            tables[i] = dataSet.getTable(i);
        }

        Table<SolutionGroupRecord> solutionTable = dataSet.getTable(0);
        return new CommonDataSet(
                tables,
                solutionTable);
    }
}
