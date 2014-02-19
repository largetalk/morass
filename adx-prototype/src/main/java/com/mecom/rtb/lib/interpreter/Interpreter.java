/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.interpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

public class Interpreter {

    private static final String LINE_SEPARATOR_REGEX = "\\n+";
    private static final String ELEMENT_SEPARATOR_REGEX = "\\s+";
    private static final String COMMENT_INDICATOR = "#";

    private Dispatcher dispatcher;

    public Interpreter(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean run(String script, boolean usesStrictMode) {
        boolean returnValue = true;
        String lineArray[] = extractLines(script);
        for (String line : lineArray) {
            String elementArray[] = extractElements(line);
            String commandName = elementArray[0];
            String parameters[] = Arrays.copyOfRange(elementArray,
                    1, elementArray.length);
            if (!dispatcher.dispatch(commandName, parameters)) {
                System.err.println("INTERPRETER.RUN: command error \""
                        + line + "\"");
                returnValue = false;
                if (usesStrictMode) {
                    break;
                }
            }
        }
        return returnValue;
    }

    public boolean run(File scriptFile, boolean usesStrictMode) {
        String script;
        try {
            script = FileUtils.readFileToString(scriptFile);
        } catch (IOException ex) {
            System.err.println("INTERPRETER.RUN: script load error "
                    + ex.toString());
            return false;
        }
        return run(script, usesStrictMode);
    }

    private static String[] extractLines(String script) {
        String lineArray[] = script.split(LINE_SEPARATOR_REGEX);
        ArrayList<String> effectiveLineList = new ArrayList<String>();
        for (String line : lineArray) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith(COMMENT_INDICATOR)) {
                effectiveLineList.add(line);
            }
        }
        return effectiveLineList.toArray(new String[0]);
    }

    private static String[] extractElements(String line) {
        String elementArray[] = line.split(ELEMENT_SEPARATOR_REGEX);
        ArrayList<String> effectiveElementList = new ArrayList<String>();
        for (String element : elementArray) {
            element = element.trim();
            if (!element.isEmpty()) {
                effectiveElementList.add(element);
            }
        }
        return effectiveElementList.toArray(new String[0]);
    }
}
