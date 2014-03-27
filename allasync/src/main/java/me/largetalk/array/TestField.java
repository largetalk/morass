/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package me.largetalk.array;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 *
 * @author largetalk
 */
public class TestField {
    public static final String HH = "hah";
    public static final String UU = "uau";
    
    public static void main(String args[]) throws IllegalArgumentException, IllegalAccessException {
        Field fields[] = TestField.class.getDeclaredFields();
        HashSet<String> keys = new HashSet<String>();
        for (Field field: fields) {
            System.out.println(field);
            int modifier = field.getModifiers();
            Class typeClass = field.getType();
            if (Modifier.isFinal(modifier)
                    && Modifier.isPublic(modifier)
                    && Modifier.isStatic(modifier)
                    && typeClass == String.class) {
                
                System.out.println(field.getName());
                System.out.println((String) field.get(null));
            }
        }
    }
}
