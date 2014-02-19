/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.cli;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class ArgumentsParser {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface MainClass {

        Class clazz();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Argument {

        String shortName();

        String longName();

        String description();
    }

    private static class FieldMeta {
        public final String shortName;
        public final String longName;
        public final String description;
        public final Field field;

        public FieldMeta(String shortName, String longName,
                String description, Field field) {
            this.shortName = shortName;
            this.longName = longName;
            this.description = description;
            this.field = field;
        }
    }

    public static ArgumentsParser parse(Class argumentsClass, String args[]) {
        if (argumentsClass == ArgumentsParser.class
                || !ArgumentsParser.class.isAssignableFrom(argumentsClass)) {
            System.err.printf(
                    "arguments class %s does not extends ArgumentParser\n",
                    argumentsClass);
            throw new IllegalArgumentException(
                    "Does not extends ArgumentParser");
        }

        if (!argumentsClass.isAnnotationPresent(MainClass.class)) {
            System.err.printf(
                    "MainClass annotation not found for "
                    + "arguments class %s\n", argumentsClass);
            throw new IllegalArgumentException(
                    "No MainClass annotaion detected");
        }

        MainClass classAnnotation =
                (MainClass) argumentsClass.getAnnotation(MainClass.class);
        String className = classAnnotation.clazz().getName();

        ArgumentsParser arguments = null;
        try {
            Constructor argumentsConstructor = argumentsClass.getConstructor();
            arguments = (ArgumentsParser) argumentsConstructor.newInstance();
        } catch (Exception ex) {
            System.err.printf("arguments object %s create failed - %s\n",
                    argumentsClass, ex);
            throw new IllegalArgumentException(
                    "Arguments object create failed");
        }

        ArrayList<FieldMeta> fieldArray = new ArrayList<FieldMeta>();
        HashMap<String, Integer> orderMap = new HashMap<String, Integer>();

        int fieldIndex = 0;
        Field fields[] = argumentsClass.getDeclaredFields();
        for (Field field : fields) {
            // Handles only public non-final non-static field
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers)
                    || Modifier.isStatic(modifiers)
                    || Modifier.isFinal(modifiers)) {
                continue;
            }
            if (!field.isAnnotationPresent(Argument.class)) {
                continue;
            }

            Argument fieldAnnotation =
                    (Argument) field.getAnnotation(Argument.class);
            fieldArray.add(new FieldMeta(fieldAnnotation.shortName(),
                    fieldAnnotation.longName(),
                    fieldAnnotation.description(), field));
            orderMap.put(fieldAnnotation.shortName(), fieldIndex);
            fieldIndex++;
        }

        Options options = createOptions(fieldArray);
        CommandLine commandLine = parseCommandLine(args, options);
        if (commandLine == null) {
            printUsage(options, className, orderMap);
            return null;
        }

        if (!assignArguments(arguments, commandLine, fieldArray)) {
            printUsage(options, className, orderMap);
            return null;
        }

        if (!arguments.convertCustomArguments()) {
            System.err.printf("some custom arguments parse failed\n");
            printUsage(options, className, orderMap);
            return null;
        }

        if (!arguments.validate()) {
            System.err.printf("some arguments are invalid\n");
            printUsage(options, className, orderMap);
            return null;
        }

        return arguments;
    }

    public static void logFormatError(
            String argument, String value, Exception ex) {
        String message = String.format(
                "error format \"%s\" for argument %s", value, argument);
        System.err.printf("%s - %s\n", message, ex);
        System.out.println(message);
    }

    public static void logInvalidValue(String argument, Object value) {
        String message = String.format(
                "invalid value \"%s\" for argument %s", value, argument);
        System.err.printf("%s\n", message);
        System.out.println(message);
    }

    protected boolean convertCustomArguments() {
        return true;
    }

    protected boolean validate() {
        return true;
    }

    private static Options createOptions(ArrayList<FieldMeta> fieldArray) {
        Options options = new Options();
        for (FieldMeta fieldMeta : fieldArray) {
            @SuppressWarnings("static-access")
            Option option = OptionBuilder
                    .withLongOpt(fieldMeta.longName)
                    .hasArg()
                    .isRequired()
                    .withDescription(fieldMeta.description)
                    .withArgName(null)
                    .create(fieldMeta.shortName);
            options.addOption(option);
        }
        return options;
    }

    private static CommandLine parseCommandLine(
            String args[], Options options) {
        CommandLine commandLine = null;
        CommandLineParser parser = new PosixParser();
        try {
            commandLine = parser.parse(options, args);
        } catch (Exception ex) {
            System.err.printf("invalid command line %s - %s\n",
                    argsToString(args), ex);
        }
        return commandLine;
    }

    private static boolean assignArguments(ArgumentsParser arguments,
            CommandLine commandLine, ArrayList<FieldMeta> fieldArray) {
        for (FieldMeta fieldMeta : fieldArray) {
            String shortName = fieldMeta.shortName;
            Class fieldType = fieldMeta.field.getType();

            String stringValue =
                    commandLine.getOptionValue(fieldMeta.shortName);
            try {
                if (fieldType == boolean.class
                        || fieldType == Boolean.class) {
                    boolean value = Boolean.parseBoolean(stringValue);
                    fieldMeta.field.set(arguments, value);
                } else if (fieldType == int.class
                        || fieldType == Integer.class) {
                    try {
                        int value = Integer.parseInt(stringValue);
                        fieldMeta.field.set(arguments, value);
                    } catch (NumberFormatException ex) {
                        logFormatError(shortName, stringValue, ex);
                        return false;
                    }
                } else if (fieldType == long.class
                        || fieldType == Long.class) {
                    try {
                        long value = Long.parseLong(stringValue);
                        fieldMeta.field.set(arguments, value);
                    } catch (NumberFormatException ex) {
                        logFormatError(shortName, stringValue, ex);
                        return false;
                    }
                } else if (fieldType == double.class
                        || fieldType == Double.class) {
                    try {
                        double value = Double.parseDouble(stringValue);
                        fieldMeta.field.set(arguments, value);
                    } catch (NumberFormatException ex) {
                        logFormatError(shortName, stringValue, ex);
                        return false;
                    }
                } else if (fieldType == String.class) {
                    String value = stringValue;
                    fieldMeta.field.set(arguments, value);
                } else if (fieldType == File.class) {
                    File value = new File(stringValue);
                    fieldMeta.field.set(arguments, value);
                } else {
                    System.err.printf("unsupported field type %s\n", fieldType);
                }
            } catch (IllegalArgumentException ex) {
                logFieldAccessError(fieldMeta.field, ex);
            } catch (IllegalAccessException ex) {
                logFieldAccessError(fieldMeta.field, ex);
            }
        }
        return true;
    }

    private static void printUsage(Options options, String className,
            HashMap<String, Integer> orderMap) {
        HelpFormatter formatter = new HelpFormatter();
        OptionComparator comparator = new OptionComparator(orderMap);
        formatter.setOptionComparator(comparator);
        formatter.printHelp(className, options);
    }

    private static void logFieldAccessError(Field field, Exception ex) {
        System.err.printf("field %s access failed - %s\n",
                field.getName(), ex);
        throw new IllegalArgumentException("Field access error");
    }

    private static String argsToString(String args[]) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            builder.append(", ").append(args[i]);
        }
        builder.append("\"");
        return builder.toString();
    }
}
