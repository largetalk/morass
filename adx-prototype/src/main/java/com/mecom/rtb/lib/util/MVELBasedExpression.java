/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Map;
import org.mvel.MVEL;

public class MVELBasedExpression<T> {

    private Object expression;

    public MVELBasedExpression(String expressionString) {
        expression = (Object) MVEL.compileExpression(expressionString);
    }

    public T getExcutedResult(Map map) {
        return (T) MVEL.executeExpression(expression, map);
    }
}
