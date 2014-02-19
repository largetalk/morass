/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demographics {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Demographics.class);

    public byte gender;
    public byte age;
    public byte income;
    public byte educate;
    public byte career;

    public Demographics() {
    }

    public Demographics(byte gender, byte age, byte income,
            byte educate, byte career) {
        this.gender = gender;
        this.age = age;
        this.income = income;
        this.educate = educate;
        this.career = career;
    }

    public byte getGender() {
        return gender;
    }

    public byte getAge() {
        return age;
    }

    public byte getIncome() {
        return income;
    }

    public byte getEducate() {
        return educate;
    }

    public byte getCareer() {
        return career;
    }

    public Map putThisToMap() {
        Map resultMap = new HashMap<String, Integer>();
        resultMap.put("gender", new Integer(gender));
        resultMap.put("age", new Integer(age));
        resultMap.put("income", new Integer(income));
        resultMap.put("educate", new Integer(educate));
        resultMap.put("career", new Integer(career));
        return resultMap;
    }

    @Override
    public String toString() {
        String string = "Demographics = { ";
        string += "gender = " + gender + ", ";
        string += "age = " + age + ", ";
        string += "income = " + income + ", ";
        string += "educate = " + educate + ", ";
        string += "career = " + career + " }";
        return string;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else if (this == object) {
            return true;
        } else if (getClass() != object.getClass()) {
            return false;
        }

        Demographics rhs = (Demographics) object;
        return new EqualsBuilder().
                append(gender, rhs.gender).
                append(age, rhs.age).
                append(income, rhs.income).
                append(educate, rhs.educate).
                append(career, rhs.career).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 97).
                append(gender).
                append(age).
                append(income).
                append(educate).
                append(career).
                toHashCode();
    }
}
