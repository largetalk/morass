/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.function;

public class LinearUnaryFunction implements UnaryFunction {

    private final double slope;
    private final double intercept;

    public LinearUnaryFunction(Point onePoint, Point anotherPoint) {
        if (onePoint.x == anotherPoint.x) {
            throw new IllegalArgumentException();
        }
        slope = (onePoint.y - anotherPoint.y) / (onePoint.x - anotherPoint.x);
        intercept = onePoint.y - slope * onePoint.x;
    }

    @Override
    public double evaluate(double x) {
        return slope * x + intercept;
    }
}
