/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.interpreter;

import java.util.HashMap;

public class Dispatcher {

    private HashMap<String, Command> commandMap;

    public Dispatcher() {
        commandMap = new HashMap<String, Command>();
    }

    public boolean register(Command command) {
        String commandName = command.getName();
        if (commandName == null || commandName.isEmpty()) {
            System.err.println("DISPATCHER.REGISTER: command name should"
                    + " contain at least one character");
            return false;
        } else if (commandMap.containsKey(commandName)) {
            System.err.println("DISPATCHER.REGISTER: command name \""
                    + commandName + "\" has been registered");
            return false;
        } else {
            commandMap.put(command.getName(), command);
            return true;
        }
    }

    public boolean dispatch(String commandName, String parameters[]) {
        if (commandName == null || commandName.isEmpty()) {
            System.err.println("DISPATCHER.DISPATCH: command name should"
                    + " contain at least one character");
            return false;
        } else if (!commandMap.containsKey(commandName)) {
            System.err.println("DISPATCHER.DISPATCH: command name \""
                    + commandName + "\" has NOT been registered");
            return false;
        } else {
            Command command = commandMap.get(commandName);
            return command.execute(parameters);
        }
    }
}
