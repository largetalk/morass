/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.ini;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IniManager {

    protected static final Logger LOGGER =
            LoggerFactory.getLogger(IniManager.class);

    private static final Pattern NAME_TYPE_PATTERN;
    private static final char REMARK_SIGN = '#';

    private LinkedHashMap<String, Property> properties;

    static {
        final String regex =
                "^\\s*\\[\\s*(\\S+)\\s*]\\s*\\(\\s*(\\w+)\\)\\s*$";
        NAME_TYPE_PATTERN = Pattern.compile(regex);
    }

    public IniManager() {
        properties = new LinkedHashMap<String, Property>();
    }

    private static abstract class Property<T> {

        protected static final int LINE_LENGTH;
        protected static final int INDENT_LENGTH;
        protected static final String INDENT;

        protected String name;
        protected String type;
        protected T value;

        static {
            LINE_LENGTH = 80;
            INDENT_LENGTH = 4;
            INDENT = new String(new char[INDENT_LENGTH]).replace("\0", " ");
        }

        public Property(String name, String type) {
            this(name, type, null);
        }

        public Property(String name, String type, T value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public static Property createInstance(String name, String type,
                ArrayList<String> valueLines) {

            if (!type.equals(TextProperty.TYPE)) {
                ArrayList<String> purified = purifyLines(valueLines);
                if (purified.isEmpty()) {
                    LOGGER.info("empty value for non-text property {}", name);
                    return null;
                }
            }

            Property property;
            if (type.equals(TextProperty.TYPE)) {
                property = new TextProperty(name);
            } else if (type.equals(StringProperty.TYPE)) {
                property = new StringProperty(name);
            } else if (type.equals(BooleanProperty.TYPE)) {
                property = new BooleanProperty(name);
            } else if (type.equals(IntegerProperty.TYPE)) {
                property = new IntegerProperty(name);
            } else if (type.equals(FloatProperty.TYPE)) {
                property = new FloatProperty(name);
            } else if (type.equals(DoubleProperty.TYPE)) {
                property = new DoubleProperty(name);
            } else if (type.equals(Base64Property.TYPE)) {
                property = new Base64Property(name);
            } else {
                LOGGER.error("unknown type {} encountered", type);
                return null;
            }

            property.parseValue(valueLines);
            return property;
        }

        public final String getName() {
            return name;
        }

        public final String getType() {
            return type;
        }

        public final T getValue() {
            return value;
        }

        public final void setValue(T value) {
            this.value = value;
        }

        public final void write(PrintWriter writer) {
            writer.printf("[%s](%s)\n", name, type);
            writeValue(writer);
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

            Property rhs = (Property) object;
            return new EqualsBuilder().
                    append(name, rhs.name).
                    append(type, rhs.type).
                    append(value, rhs.value).
                    isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(57, 79).
                    append(name).
                    append(type).
                    append(value).
                    toHashCode();
        }

        public abstract void parseValue(ArrayList<String> valueLines);

        protected static String purifyLine(String line) {
            return line.trim();
        }

        protected static ArrayList<String> purifyLines(
                ArrayList<String> valueLines) {

            ArrayList<String> purified =
                    new ArrayList<String>(valueLines.size());
            for (String line : valueLines) {
                String clean = purifyLine(line);
                if (!clean.isEmpty()) {
                    purified.add(clean);
                }
            }
            return purified;
        }

        protected abstract void writeValue(PrintWriter writer);
    }

    private static class TextProperty extends Property<String> {

        public static final String TYPE = "text";

        private TextProperty(String name) {
            super(name, TYPE);
        }

        public TextProperty(String name, String value) {
            super(name, TYPE, value);
        }

        @Override
        public void parseValue(ArrayList<String> valueLines) {
            value = StringUtils.join(valueLines, '\n');
            if (valueLines.size() > 0) {
                value += '\n';
            }
        }

        @Override
        protected void writeValue(PrintWriter writer) {
            writer.printf("%s", value);
        }
    }

    private static abstract class SimpleProperty<T> extends Property<T> {

        public SimpleProperty(String name, String type) {
            super(name, type);
        }

        public SimpleProperty(String name, String type, T value) {
            super(name, type, value);
        }

        @Override
        public void parseValue(ArrayList<String> valueLines) {
            valueLines = purifyLines(valueLines);
            int numLines = valueLines.size();
            if (numLines != 1) {
                LOGGER.error("line number {} is not 1 for a simple property",
                        numLines);
                return;
            }
            parseValue(valueLines.get(0));
        }

        @Override
        protected void writeValue(PrintWriter writer) {
            writer.printf("%s%s\n", INDENT, value);
        }

        protected abstract void parseValue(String text);
    }

    private static class StringProperty extends SimpleProperty<String> {

        public static final String TYPE = "string";

        public StringProperty(String name) {
            super(name, TYPE);
        }

        public StringProperty(String name, String value) {
            super(name, TYPE, value);
        }

        @Override
        protected void parseValue(String text) {
            value = text;
        }
    }

    private static class BooleanProperty extends SimpleProperty<Boolean> {

        public static final String TYPE = "boolean";

        public BooleanProperty(String name) {
            super(name, TYPE);
        }

        public BooleanProperty(String name, Boolean value) {
            super(name, TYPE, value);
        }

        @Override
        protected void parseValue(String text) {
            value = Boolean.parseBoolean(text);
        }
    }

    private static class IntegerProperty extends SimpleProperty<Integer> {

        public static final String TYPE = "integer";

        public IntegerProperty(String name) {
            super(name, TYPE);
        }

        public IntegerProperty(String name, Integer value) {
            super(name, TYPE, value);
        }

        @Override
        protected void parseValue(String text) {
            value = Integer.parseInt(text);
        }
    }

    private static class FloatProperty extends SimpleProperty<Float> {

        public static final String TYPE = "float";

        public FloatProperty(String name) {
            super(name, TYPE);
        }

        public FloatProperty(String name, Float value) {
            super(name, TYPE, value);
        }

        @Override
        protected void parseValue(String text) {
            value = Float.parseFloat(text);
        }
    }

    private static class DoubleProperty extends SimpleProperty<Double> {

        public static final String TYPE = "double";

        public DoubleProperty(String name) {
            super(name, TYPE);
        }

        public DoubleProperty(String name, Double value) {
            super(name, TYPE, value);
        }

        @Override
        protected void parseValue(String text) {
            value = Double.parseDouble(text);
        }
    }

    private static class Base64Property extends Property<byte[]> {

        public static final String TYPE = "base64";
        protected static final int MAX_LINE_LENGTH = 40;

        public Base64Property(String name) {
            super(name, TYPE);
        }

        public Base64Property(String name, byte value[]) {
            super(name, TYPE, value);
        }

        @Override
        public void parseValue(ArrayList<String> valueLines) {
            valueLines = purifyLines(valueLines);
            String base64String = StringUtils.join(valueLines.toArray());
            value = Base64.decodeBase64(base64String);
        }

        @Override
        protected void writeValue(PrintWriter writer) {
            byte encoded[] = Base64.encodeBase64(value);
            int dataLength = encoded.length;
            int snippetLength = LINE_LENGTH - INDENT_LENGTH;
            for (int i = 0; i < encoded.length; i += snippetLength) {
                String snippet = new String(encoded, i,
                        Math.min(snippetLength, dataLength - i));
                writer.printf("%s%s\n", INDENT, snippet);
            }
        }
    }

    public void clearProperties() {
        properties.clear();
    }

    public int getNumProperties() {
        return properties.size();
    }

    public String getTextProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(TextProperty.class)) {
            return (String) property.value;
        } else {
            return null;
        }
    }

    public boolean setTextProperty(String name, String value) {
        if (properties.containsKey(name)) {
            return false;
        }
        TextProperty property = new TextProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public String getStringProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(StringProperty.class)) {
            return (String) property.value;
        } else {
            return null;
        }
    }

    public boolean setStringProperty(String name, String value) {
        if (properties.containsKey(name)) {
            return false;
        }
        StringProperty property = new StringProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public Boolean getBooleanProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(BooleanProperty.class)) {
            return (Boolean) property.value;
        } else {
            return null;
        }
    }

    public boolean setBooleanProperty(String name, Boolean value) {
        if (properties.containsKey(name)) {
            return false;
        }
        BooleanProperty property = new BooleanProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public Integer getIntegerProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(IntegerProperty.class)) {
            return (Integer) property.value;
        } else {
            return null;
        }
    }

    public boolean setIntegerProperty(String name, Integer value) {
        if (properties.containsKey(name)) {
            return false;
        }
        IntegerProperty property = new IntegerProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public Float getFloatProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(FloatProperty.class)) {
            return (Float) property.value;
        } else {
            return null;
        }
    }

    public boolean setFloatProperty(String name, Float value) {
        if (properties.containsKey(name)) {
            return false;
        }
        FloatProperty property = new FloatProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public Double getDoubleProperty(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(DoubleProperty.class)) {
            return (Double) property.value;
        } else {
            return null;
        }
    }

    public boolean setDoubleProperty(String name, Double value) {
        if (properties.containsKey(name)) {
            return false;
        }
        DoubleProperty property = new DoubleProperty(name, value);
        properties.put(name, property);
        return true;
    }

    public Base64 getBase64Property(String name) {
        Property property = getProperty(name);
        if (property == null) {
            return null;
        } else if (property.getClass().equals(Base64Property.class)) {
            return (Base64) property.value;
        } else {
            return null;
        }
    }

    public boolean setBase64Property(String name, byte value[]) {
        if (properties.containsKey(name)) {
            return false;
        }
        Base64Property property = new Base64Property(name, value);
        properties.put(name, property);
        return true;
    }

    public boolean deleteProperty(String name) {
        if (!properties.containsKey(name)) {
            return false;
        }
        properties.remove(name);
        return true;
    }

    public void load(String filePath) {
        File file = new File(filePath);
        load(file);
    }

    public void load(File file) {
        clear();

        ArrayList<String> lineArray = readLines(file);
        ArrayList<String> valueLines = new ArrayList<String>();
        StringProperty curNameType = null;
        for (String line : lineArray) {
            StringProperty nameType = parseNameType(line);
            if (nameType != null) {
                if (curNameType != null) {
                    addProperty(curNameType.name, curNameType.value,
                            valueLines);
                } else if (containsNonSpace(valueLines)) {
                    LOGGER.error("non-empty lines before the first property");
                }
                curNameType = nameType;
                valueLines.clear();
                continue;
            }

            valueLines.add(line);
        }
        if (curNameType != null) {
            addProperty(curNameType.name, curNameType.value, valueLines);
        }
    }

    public void save(String filePath) {
        File file = new File(filePath);
        save(file);
    }

    public void save(File file) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(file));
            BufferedWriter bufferedWriter =
                    new BufferedWriter(outputStreamWriter);
            PrintWriter writer = new PrintWriter(bufferedWriter);

            for (Property property : properties.values()) {
                property.write(writer);
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error("open file {} failure", file, ex);
        } finally {
            try {
                outputStreamWriter.close();
            } catch (IOException ex) {
                LOGGER.error("close file {} failure", file, ex);
            }
        }
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

        IniManager rhs = (IniManager) object;
        return new EqualsBuilder().
                append(properties, rhs.properties).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(61, 33).
                append(properties).
                toHashCode();
    }

    private static ArrayList<String> readLines(File file) {
        ArrayList<String> lineArray = new ArrayList<String>();
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(
                    new FileInputStream(file));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                int position = line.indexOf(REMARK_SIGN);
                if (position == 0) {
                    continue;
                } else if (position > 0) {
                    line = line.substring(0, position);
                }
                lineArray.add(line);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("file {} not exist", file, ex);
        } catch (IOException ex) {
            LOGGER.error("read from file {} failure", file, ex);
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ex) {
                LOGGER.error("close file {} failure", file, ex);
            }
        }
        return lineArray;
    }

    private Property getProperty(String propertyName) {
        if (!properties.containsKey(propertyName)) {
            return null;
        } else {
            return properties.get(propertyName);
        }
    }

    private static StringProperty parseNameType(String line) {
        Matcher matcher = NAME_TYPE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        } else {
            return new StringProperty(matcher.group(1), matcher.group(2));
        }
    }

    private static boolean containsNonSpace(ArrayList<String> valueLines) {
        for (String line : valueLines) {
            if (!line.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void clear() {
        properties.clear();
    }

    private void addProperty(String name, String type,
            ArrayList<String> valueLines) {

        if (properties.containsKey(name)) {
            LOGGER.error("duplicated property {} encountered", name);
            return;
        }

        Property property = Property.createInstance(name, type, valueLines);
        if (property != null) {
            properties.put(name, property);
        }
    }
}
