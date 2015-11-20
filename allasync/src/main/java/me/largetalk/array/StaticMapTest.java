/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.array;

import java.util.Arrays;
import java.util.HashSet;

public class StaticMapTest {

    private static final HashSet<Integer> INTEGER_PLACE_HOLD_SET = new HashSet<Integer>();
    private static final HashSet<String> STRING_PLACE_HOLD_SET = new HashSet<String>();
    
    
    private HashSet<Integer> excludedCategorySet = INTEGER_PLACE_HOLD_SET;
    private HashSet<String> excludedadvertiserIdSet = STRING_PLACE_HOLD_SET;
    private HashSet<String> excludedClickThroughUrlSet = STRING_PLACE_HOLD_SET;	
    private HashSet<Integer> excludedFilterSet = INTEGER_PLACE_HOLD_SET;

    public StaticMapTest() {
        System.out.println(excludedCategorySet);
        System.out.println(excludedClickThroughUrlSet);
        System.out.println(excludedFilterSet);
        System.out.println(excludedadvertiserIdSet);
        
        
        excludedCategorySet.addAll(Arrays.asList(new Integer[]{1,2,3}));
        excludedFilterSet.addAll(Arrays.asList(new Integer[]{3, 4, 5}));
        
        excludedadvertiserIdSet.addAll(Arrays.asList(new String[]{"aa", "bb"}));
        excludedClickThroughUrlSet.addAll(Arrays.asList(new String[]{"cc", "dd"}));
    }
    
    public static void main(String args[]) {
        StaticMapTest smt = new StaticMapTest();
        System.out.println(smt.excludedCategorySet);
        System.out.println(smt.excludedClickThroughUrlSet);
        System.out.println(smt.excludedFilterSet);
        System.out.println(smt.excludedadvertiserIdSet);
        
        System.out.println("###############");
        
        StaticMapTest smt2 = new StaticMapTest();
        System.out.println(smt2.excludedCategorySet);
        System.out.println(smt2.excludedClickThroughUrlSet);
        System.out.println(smt2.excludedFilterSet);
        System.out.println(smt2.excludedadvertiserIdSet);
    }
}
