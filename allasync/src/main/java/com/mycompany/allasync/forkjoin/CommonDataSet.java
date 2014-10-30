/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.smartbid.common.dataset;

import com.adsame.rtb.lib.dataset.DataSet;
import com.adsame.rtb.lib.dataset.Table;

public class CommonDataSet extends DataSet {

    protected final Table<SolutionGroupRecord> solutionGroupTable;


    public CommonDataSet(
            Table tables[],
            Table<SolutionGroupRecord> solutionTable) {
        super(tables);
        this.solutionGroupTable = solutionTable;
    }

    public boolean isEmptySolutionGroupTable() {
        return solutionGroupTable.getNumRecords() == 0;
    }

    public Table<SolutionGroupRecord> getSolutionTable() {
        return solutionGroupTable;
    }
}
