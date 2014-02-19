/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitializableLoader<V extends Initializable> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitializableLoader.class);

    private static String CLASS_NAME_FILE = "classname";
    private static String CONFIGURATION_FILE = "configuration";

    private File rootDir;

    public InitializableLoader(String rootDirPath) {
        this(new File(rootDirPath));
    }

    public InitializableLoader(File rootDir) {
        this.rootDir = rootDir;
    }

    public V load(String name) {
        return load(name, null);
    }

    public V load(String name, V currentInstance) {
        File subDir = new File(rootDir, name);

        String className = getClassName(subDir);
        if (className == null) {
            LOGGER.error("get class name from {} failed", subDir);
            return null;
        }

        Configuration configuration = getConfiguration(subDir);
        if (configuration == null) {
            LOGGER.error("get configuration from {} failed", subDir);
            return null;
        }

        V instance;
        try {
            if (currentInstance == null ||
                    !currentInstance.getClass().getName().equals(className)) {
                Class classObject = Class.forName(className);
                instance = (V) classObject.newInstance();
            } else {
                instance = currentInstance;
            }
            instance.initialize(configuration);
        } catch (Exception ex) {
            LOGGER.error("load instance from {} failed", subDir, ex);
            return null;
        }

        return instance;
    }

    private String getClassName(File subDir) {
        File classNameFile = new File(subDir, CLASS_NAME_FILE);
        String className = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(classNameFile));
            className = reader.readLine();
        } catch (IOException ex) {
            LOGGER.error("read {} failed", classNameFile, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.error("close {} failed", classNameFile, ex);
                }
            }
        }
        return className;
    }

    private Configuration getConfiguration(File subDir) {
        File configurationFile = new File(subDir, CONFIGURATION_FILE);
        Configuration configuration = new Configuration();
        try {
            configuration.load(configurationFile);
        } catch (IOException ex) {
            LOGGER.error("load {} failed", configurationFile, ex);
            return null;
        }
        return configuration;
    }
}
