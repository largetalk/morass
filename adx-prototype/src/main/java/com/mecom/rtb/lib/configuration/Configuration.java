/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.configuration;

import com.adsame.rtb.lib.util.LinkedProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Configuration.class);

    // Add a <Annotation.class, Parameter.class> entry for each data type
    // Currently we support 10 data types:
    //     1.  Boolean      <BooleanAnnotation.class, BooleanParameter.class>
    //     2.  Byte         <ByteAnnotation.class, ByteParameter.class>
    //     3.  Short        <ShortAnnotation.class, ShortParameter.class>
    //     4.  Integer      <IntegerAnnotation.class, IntegerParameter.class>
    //     5.  Long         <LongAnnotation.class, LongParameter.class>
    //     6.  Float        <FloatAnnotation.class, FloatParameter.class>
    //     7.  Double       <DoubleAnnotation.class, DoubleParameter.class>
    //     8.  String       <StringAnnotation.class, StringParameter.class>
    //     9.  StringArray  <StringArrayAnnotation.class, StringArrayParameter.class>
    //     10. Link         <LinkAnnotation.class, LinkParameter.class>
    private static final HashMap<Class, Class> CLASS_MAP;

    private String path;
    private LinkedHashMap<String, Class> metaMap;
    private LinkedHashMap<String, LinkedHashMap<String, String>> sectionMap;

    static {
        CLASS_MAP = new HashMap<Class, Class>();
        CLASS_MAP.put(BooleanAnnotation.class, BooleanParameter.class);
        CLASS_MAP.put(ByteAnnotation.class, ByteParameter.class);
        CLASS_MAP.put(ShortAnnotation.class, ShortParameter.class);
        CLASS_MAP.put(IntegerAnnotation.class, IntegerParameter.class);
        CLASS_MAP.put(LongAnnotation.class, LongParameter.class);
        CLASS_MAP.put(FloatAnnotation.class, FloatParameter.class);
        CLASS_MAP.put(DoubleAnnotation.class, DoubleParameter.class);
        CLASS_MAP.put(StringAnnotation.class, StringParameter.class);
        CLASS_MAP.put(StringArrayAnnotation.class, StringArrayParameter.class);
        CLASS_MAP.put(LinkAnnotation.class, LinkParameter.class);
    }

    public Configuration() {
        path = new File(".").getParent();
        metaMap = new LinkedHashMap<String, Class>();
        sectionMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
    }

    public static class Section {

        private static final Logger LOGGER =
                LoggerFactory.getLogger(Section.class);

        private static final String INDENT = "  ";

        private final Configuration configuration;
        private String comment;
        private String prefix;
        private LinkedHashMap<String, Parameter> parameterMap;

        private Section(Configuration configuration, Class metaClass) {
            this.configuration = configuration;

            synchronized(configuration) {
                LinkedHashMap<String, Class> metaMap = configuration.metaMap;
                LinkedHashMap<String, LinkedHashMap<String, String>>
                        sectionMap = configuration.sectionMap;
                processMetaAnnotation(metaClass);

                // Sections with the same name but different meta are invalid
                if (metaMap.containsKey(prefix)
                        && metaMap.get(prefix) != metaClass) {
                    LOGGER.error("config meta {} conflicts existing meta {}",
                            metaClass.getName(), metaMap.get(prefix).getName());
                    throw new IllegalArgumentException();
                }

                metaMap.put(prefix, metaClass);

                if (!sectionMap.containsKey(prefix)) {
                    sectionMap.put(prefix, new LinkedHashMap<String, String>());
                }

                processParameterAnnotation(metaClass);
            }
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public String getPath() {
            String path;
            synchronized(configuration) {
                path = configuration.path;
            }
            return path;
        }

        public boolean contains(String key) {
            boolean bool;
            synchronized(configuration) {
                bool = parameterMap.containsKey(key);
            }
            return bool;
        }

        public Object get(String key) {
            Object object;
            synchronized(configuration) {
                if (!parameterMap.containsKey(key)) {
                    LOGGER.error("config key {} not found", key);
                    throw new IllegalArgumentException();
                }
                object = parameterMap.get(key).getValue();
            }
            return object;
        }

        public void set(String key, Object value) {
            synchronized(configuration) {
                if (!parameterMap.containsKey(key) || value == null) {
                    LOGGER.error("config key {} or value {} are invalid",
                            key, value);
                    throw new IllegalArgumentException();
                }
                parameterMap.get(key).setValue(value);
            }
        }

        public void save(String filePath, int level) throws IOException {
            File file = new File(filePath);
            save(file, level);
        }

        public void save(File file, int level) throws IOException {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            save(fileOutputStream, level);
            fileOutputStream.close();
        }

        public void save(OutputStream outputStream, int level)
                throws IOException{
            synchronized(configuration) {
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream));
                String fullPrefix = prefix + ".";
                String indent = StringUtils.repeat(INDENT, level);
                writer.write(indent);
                writer.write("# " + comment);
                writer.write("\n");
                for (String parameterName : parameterMap.keySet()) {
                    Parameter parameter = parameterMap.get(parameterName);
                    Object parameterValue = parameter.getValue();
                    writer.write(indent);
                    writer.write("# " + parameter.getComment());
                    writer.write("\n");
                    writer.write(indent);
                    writer.write(fullPrefix + parameterName + " = ");
                    writer.write(parameter.valueToString(parameterValue));
                    writer.write("\n");
                }
                writer.write("\n");
                writer.flush();
            }
        }

        private void processMetaAnnotation(Class metaClass) {
            if (!metaClass.isAnnotationPresent(MetaAnnotation.class)) {
                LOGGER.error("invalid meta class {}", metaClass);
                throw new IllegalArgumentException(
                        "No meta annotaion detected");
            }
            MetaAnnotation annotation = (MetaAnnotation)
                    metaClass.getAnnotation(MetaAnnotation.class);
            comment = annotation.comment();
            prefix = annotation.prefix();
        }

        private void processParameterAnnotation(Class metaClass) {
            parameterMap = new LinkedHashMap<String, Parameter>();

            // Handles only declared fields
            Field fields[] = metaClass.getDeclaredFields();
            for (Field field : fields) {

                // Handles only fields of "public static final String"
                int modifiers = field.getModifiers();
                Class typeClass = field.getType();
                if (!Modifier.isPublic(modifiers)
                        || !Modifier.isStatic(modifiers)
                        || !Modifier.isFinal(modifiers)
                        || typeClass != String.class) {
                    continue;
                }

                // Handles only fields with exactly one parameter annotation
                // and any number of other annotations.
                //
                // Fields with more than one parameter annotation will trigger
                // an exception.
                //
                // Fields with no parameter annotation will be ignored.
                boolean found = false;
                Annotation targetAnnotation = null;
                for (Annotation annotation : field.getAnnotations()) {
                    Class annotationClass = annotation.annotationType();
                    if (CLASS_MAP.containsKey(annotationClass)) {
                        if (found) {
                            throw new IllegalArgumentException(
                                    "Multiple parameter annotations detected");
                        } else {
                            found = true;
                            targetAnnotation = annotation;
                        }
                    }
                }
                if (!found) {
                    continue;
                }

                try {
                    Class parameterClass = CLASS_MAP.get(
                            targetAnnotation.annotationType());
                    Constructor parameterConstructor =
                            parameterClass.getConstructor(
                            configuration.getClass());
                    Parameter parameter =
                            (Parameter) parameterConstructor.newInstance(
                            configuration);
                    String parameterName = (String) field.get(null);
                    parameter.initialize(configuration, targetAnnotation,
                            prefix, parameterName);
                    parameterMap.put(parameterName, parameter);
                } catch (Exception ex) {
                    LOGGER.error("config field annotation parsing error for {}",
                            metaClass, ex);
                    throw new IllegalArgumentException(
                            "Parameter initialization failed");
                }
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface MetaAnnotation {

        String prefix() default "";

        String comment() default "configuration";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface BooleanAnnotation {

        boolean defaultValue() default false;

        String comment() default "boolean";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ByteAnnotation {

        byte defaultValue() default 0;

        byte max() default Byte.MAX_VALUE;

        byte min() default Byte.MIN_VALUE;

        String comment() default "byte";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface ShortAnnotation {

        short defaultValue() default 0;

        short max() default Short.MAX_VALUE;

        short min() default Short.MIN_VALUE;

        String comment() default "short";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface IntegerAnnotation {

        int defaultValue() default 0;

        int max() default Integer.MAX_VALUE;

        int min() default Integer.MIN_VALUE;

        String comment() default "integer";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface LongAnnotation {

        long defaultValue() default 0;

        long max() default Long.MAX_VALUE;

        long min() default Long.MIN_VALUE;

        String comment() default "long";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface FloatAnnotation {

        float defaultValue() default 0.0f;

        float max() default Float.POSITIVE_INFINITY;

        float min() default Float.NEGATIVE_INFINITY;

        String comment() default "float";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface DoubleAnnotation {

        double defaultValue() default 0.0;

        double max() default Double.POSITIVE_INFINITY;

        double min() default Double.NEGATIVE_INFINITY;

        String comment() default "double";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface StringAnnotation {

        String defaultValue() default "";

        String pattern() default ".*";

        int minLength() default 0;

        int maxLength() default 1024;

        String comment() default "string";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface StringArrayAnnotation {

        String defaultValue() default "";

        String separator() default "|";

        String comment() default "string array";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface LinkAnnotation {

        String defaultValue() default "";

        String comment() default "link";
    }

    private static abstract class Parameter<T, A extends Annotation> {

        protected static final Logger LOGGER =
                LoggerFactory.getLogger(Parameter.class);

        protected String comment;
        protected String prefix;
        protected String suffix;

        protected String className;
        protected LinkedHashMap<String, LinkedHashMap<String, String>>
                sectionMap;

        public final void initialize(Configuration configuration,
                A annotation, String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;

            this.sectionMap = configuration.sectionMap;
            this.className = getClass().getSimpleName();

            internalInitialize(annotation);
        }

        public final String getComment() {
            return comment;
        }

        public void setValue(T value) {
            String string = valueToString(value);
            sectionMap.get(prefix).put(suffix, string);
        }

        public T getValue() {
            String string = sectionMap.get(prefix).get(suffix);
            return stringToValue(string);
        }

        public String valueToString(T value) {
            return value.toString();
        }

        public final T stringToValue(String string) {
            T value = null;
            try {
                value = internalStringToValue(string);
            } catch (Exception ex) {
                LOGGER.error("{} - parsing value {} failed",
                        new Object[]{className, string, ex});
                throw new IllegalArgumentException();
            }
            return value;
        }

        protected abstract void internalInitialize(A annotation);

        protected abstract T internalStringToValue(String string)
                throws Exception;
    }

    private abstract class
            NumericParameter<T extends Comparable, A extends Annotation>
            extends Parameter<T, A> {

        protected T min;
        protected T max;

        @Override
        public void setValue(T value) {
            if (value.compareTo(min) < 0 || value.compareTo(max) >= 0) {
                LOGGER.error("{} - value {} to be set not in range [{}, {})",
                        new Object[] { className, value, min, max});
                throw new IllegalArgumentException();
            }

            super.setValue(value);
        }
    }

    private final class BooleanParameter
            extends Parameter<Boolean, BooleanAnnotation> {

        public BooleanParameter() {
        }

        @Override
        protected void internalInitialize(BooleanAnnotation annotation) {
            comment = annotation.comment();
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Boolean.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Boolean internalStringToValue(String string) {
            Boolean value = Boolean.parseBoolean(string);
            return value;
        }
    }

    private final class ByteParameter
            extends NumericParameter<Byte, ByteAnnotation> {

        public ByteParameter() {
        }

        @Override
        protected void internalInitialize(ByteAnnotation annotation) {
            comment = annotation.comment();
            min = Byte.valueOf(annotation.min());
            max = Byte.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Byte.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Byte internalStringToValue(String string) {
            Byte value = Byte.parseByte(string);
            return value;
        }
    }

    private final class ShortParameter
            extends NumericParameter<Short, ShortAnnotation> {

        public ShortParameter() {
        }

        @Override
        protected void internalInitialize(ShortAnnotation annotation) {
            comment = annotation.comment();
            min = Short.valueOf(annotation.min());
            max = Short.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Short.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Short internalStringToValue(String string) {
            Short value = Short.parseShort(string);
            return value;
        }
    }

    private final class IntegerParameter
            extends NumericParameter<Integer, IntegerAnnotation> {

        public IntegerParameter() {
        }

        @Override
        protected void internalInitialize(IntegerAnnotation annotation) {
            comment = annotation.comment();
            min = Integer.valueOf(annotation.min());
            max = Integer.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Integer.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Integer internalStringToValue(String string) {
            Integer value = Integer.parseInt(string);
            return value;
        }
    }

    private final class LongParameter
            extends NumericParameter<Long, LongAnnotation> {

        public LongParameter() {
        }

        @Override
        protected void internalInitialize(LongAnnotation annotation) {
            comment = annotation.comment();
            min = Long.valueOf(annotation.min());
            max = Long.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Long.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Long internalStringToValue(String string) {
            Long value = Long.parseLong(string);
            return value;
        }
    }

    private final class FloatParameter
            extends NumericParameter<Float, FloatAnnotation> {

        public FloatParameter() {
        }

        @Override
        protected void internalInitialize(FloatAnnotation annotation) {
            comment = annotation.comment();
            min = Float.valueOf(annotation.min());
            max = Float.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Float.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Float internalStringToValue(String string) {
            Float value = Float.parseFloat(string);
            return value;
        }
    }

    private final class DoubleParameter
            extends NumericParameter<Double, DoubleAnnotation> {

        public DoubleParameter() {
        }

        @Override
        protected void internalInitialize(DoubleAnnotation annotation) {
            comment = annotation.comment();
            min = Double.valueOf(annotation.min());
            max = Double.valueOf(annotation.max());
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(Double.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Double internalStringToValue(String string) {
            Double value = Double.parseDouble(string);
            return value;
        }
    }

    private final class StringParameter
            extends Parameter<String, StringAnnotation> {

        private int minLength;
        private int maxLength;
        private String pattern;

        public StringParameter() {
        }

        @Override
        public void setValue(String value) {
            if (value.length() < minLength || value.length() >= maxLength) {
                LOGGER.error("{} - length of value {} to be set not "
                        + "in range [{}, {})",
                        new Object[]{className, value, minLength, maxLength});
                throw new IllegalArgumentException();
            } else if (!value.matches(pattern)) {
                LOGGER.error("{} - invalid value {} to be set",
                        className, value);
                throw new IllegalArgumentException();
            }

            super.setValue(value);
        }

        @Override
        protected void internalInitialize(StringAnnotation annotation) {
            comment = annotation.comment();
            minLength = annotation.minLength();
            maxLength = annotation.maxLength();
            pattern = annotation.pattern();
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(String.valueOf(annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected String internalStringToValue(String string) {
            return string;
        }
    }

    private final class StringArrayParameter
            extends Parameter<String[], StringArrayAnnotation> {

        private String separator;

        public StringArrayParameter() {
        }

        @Override
        public String valueToString(String stringArray[]) {
            StringBuilder stringBuilder = new StringBuilder();
            if (stringArray.length >= 1) {
                stringBuilder.append(stringArray[0]);
            }
            for (int i = 1; i < stringArray.length; i++) {
                stringBuilder.append(separator).append(stringArray[i]);
            }
            return stringBuilder.toString();
        }

        @Override
        protected void internalInitialize(StringArrayAnnotation annotation) {
            comment = annotation.comment();
            separator = annotation.separator();
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(split(annotation.defaultValue(), separator));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected String[] internalStringToValue(String string)
                throws Exception {
            return split(string, separator);
        }

        private String[] split(String string, String separator) {
            int startIndex = 0;
            int endIndex;
            String subString;
            ArrayList<String> stringArray = new ArrayList<String>();
            string = string.trim();
            while ((endIndex = string.indexOf(separator, startIndex)) >= 0) {
                subString = string.substring(startIndex, endIndex);
                subString = subString.trim();
                if (!subString.isEmpty()) {
                    stringArray.add(subString);
                }
                startIndex = endIndex + separator.length();
            }
            subString = string.substring(startIndex);
            subString = subString.trim();
            if (!subString.isEmpty()) {
                stringArray.add(subString);
            }
            return stringArray.toArray(new String[0]);
        }
    }

    private final class LinkParameter
            extends Parameter<Link, LinkAnnotation> {

        public LinkParameter() {
        }

        @Override
        public void setValue(Link link) {
            String subPath = link.getPath();
            if (subPath == null) {
                LOGGER.error("{} - invalid path {} to be set",
                        className, subPath);
                throw new IllegalArgumentException();
            }

            super.setValue(new Link(path, subPath));
        }

        @Override
        public Link getValue() {
            Link link = super.getValue();
            return new Link(path, link.getPath());
        }

        @Override
        public String valueToString(Link link) {
            return link.getPath();
        }

        @Override
        protected void internalInitialize(LinkAnnotation annotation) {
            comment = annotation.comment();
            if (!sectionMap.get(prefix).containsKey(suffix)) {
                setValue(new Link(path, annotation.defaultValue()));
            } else {
                setValue(getValue());
            }
        }

        @Override
        protected Link internalStringToValue(String string) {
            return new Link(path, string);
        }
    }

    public synchronized Initializable createInstance(String className) {
        Initializable instance;
        try {
            Class classObject = Class.forName(className);
            instance = (Initializable) classObject.newInstance();
            instance.initialize(this);
        } catch (Exception ex) {
            LOGGER.error("instance of class {} create failed",
                    className, ex);
            instance = null;
        }
        return instance;
    }

    public synchronized Section getSection(Class classObject) {
        return new Section(this, classObject);
    }

    public synchronized void load(String filePath) throws IOException {
        File file = new File(filePath);
        load(file);
    }

    public synchronized void load(File file) throws IOException {
        path = file.getParent();
        FileInputStream inputStream = new FileInputStream(file);
        load(inputStream);
        inputStream.close();
    }

    public synchronized void load(InputStream inputStream) throws IOException {
        LinkedProperties properties = new LinkedProperties();
        properties.load(inputStream);
        String propertyKeys[] =
                properties.getLinkedKeySet().toArray(new String[0]);
        for (String propertyKey : propertyKeys) {
            String prefix = extractPrefix(propertyKey);
            String suffix = extractSuffix(propertyKey);
            String value = properties.getProperty(propertyKey);
            LinkedHashMap<String, String> map;
            if (!sectionMap.containsKey(prefix)) {
                map = new LinkedHashMap<String, String>();
                sectionMap.put(prefix, map);
            } else {
                map = sectionMap.get(prefix);
            }
            map.put(suffix, value);
        }
    }

    public synchronized void save(String filePath) throws IOException {
        File file = new File(filePath);
        save(file);
    }

    public synchronized void save(File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        save(outputStream);
        outputStream.close();
    }

    public synchronized void save(OutputStream outputStream)
            throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        for (String prefix : sectionMap.keySet()) {
            for (String suffix : sectionMap.get(prefix).keySet()) {
                bufferedWriter.write(prefix + "." + suffix + " = ");
                bufferedWriter.write(sectionMap.get(prefix).get(suffix) + "\n");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
    }

    private static String extractPrefix(String key) {
        if (!isValidKey(key)) {
            LOGGER.error("invalid config key {}", key);
            throw new IllegalArgumentException("Invalid key!");
        }
        int index = key.indexOf('.');
        return key.substring(0, index);
    }

    private static String extractSuffix(String key) {
        if (!isValidKey(key)) {
            LOGGER.error("invalid config key {}", key);
            throw new IllegalArgumentException("Invalid key!");
        }
        int index = key.indexOf('.');
        return key.substring(index + 1);
    }

    private static boolean isValidKey(String key) {
        if (key == null) {
            return false;
        }

        int index = key.indexOf('.');
        if (index <= 0) {
            return false;
        } else if (index >= key.length() - 1) {
            return false;
        } else {
            return true;
        }
    }
}
