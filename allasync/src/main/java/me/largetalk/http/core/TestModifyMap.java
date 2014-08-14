/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.http.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestModifyMap {

    public static void main(String args[]) {
        HashMap<String, double[]> x = new HashMap<String, double[]>();
        x.put("1", new double[]{1d, 1d, 1d});
        x.put("2", new double[]{2d, 1d, 1d});
        x.put("3", new double[]{3d, 1d, 1d});
        x.put("4", new double[]{4d, 1d, 1d});
        Iterator<Map.Entry<String, double[]>> it = x.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, double[]> entry = it.next();
            System.out.println(entry.getKey());
            double plan[] = entry.getValue();
            for (double p : plan) {
                System.out.print(p + ", ");
            }
            System.out.println("");

            plan[2] = 9d;
        }

        System.out.println("####################");

        Iterator<Map.Entry<String, double[]>> it2 = x.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, double[]> entry = it2.next();
            System.out.println(entry.getKey());
            double plan2[] = entry.getValue();
            for (double p : plan2) {
                System.out.print(p + ", ");
            }
            System.out.println("");

        }

    }
}