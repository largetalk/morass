/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.function;

public class SegmentedLinearUnaryFunction implements UnaryFunction {

    private final Point leftPoint;
    private final Point rightPoint;
    private final LinearUnaryFunction centralSegmentFuction;

    public SegmentedLinearUnaryFunction(Point onePoint, Point anotherPoint) {
        if (onePoint.x < anotherPoint.x) {
            leftPoint = onePoint;
            rightPoint = anotherPoint;
        } else if (onePoint.x > anotherPoint.x) {
            leftPoint = anotherPoint;
            rightPoint = onePoint;
        } else {
            throw new IllegalArgumentException();
        }
        centralSegmentFuction = new LinearUnaryFunction(onePoint, anotherPoint);
    }

    @Override
    public double evaluate(double x) {
        if (x < leftPoint.x) {
            return leftPoint.y;
        } else if (x > rightPoint.x) {
            return rightPoint.y;
        } else {
            return centralSegmentFuction.evaluate(x);
        }
    }
}
