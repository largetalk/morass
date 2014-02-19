/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringArrayAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.IntegerAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MongoClientFactory
    implements ObjectFactory<MongoClient, Object>, Initializable {

    private static final String COLON = ":";

    private Section section;
    private String[] serverList;
    private Integer numConnections;
    private Integer multiplier;
    private ReadPreferenceEnum readPreferenceEnum;

    public MongoClientFactory() {
        this(null, null, null, ReadPreferenceEnum.NEAREST);
    }

    public MongoClientFactory(String[] serverList, Integer numConnections,
            Integer multiplier, ReadPreferenceEnum readPreferenceEnum) {
        this.serverList = serverList;
        this.numConnections = numConnections;
        this.multiplier = multiplier;
        this.readPreferenceEnum = readPreferenceEnum;
    }

    @MetaAnnotation(
            prefix = "mongo-client-factory",
            comment = "mongo client factory configuration")
    public static final class Meta {

        @StringArrayAnnotation(
                comment = "The list of mongo db servers")
        public static final String SERVERS_LIST = "serversList";

        @StringAnnotation(
                defaultValue = "nearest",
                comment = "preferred replica set members to which query be sent."
                        + "choices can be nearest/primary/primaryPreferred/"
                        + "secondary/secondaryPreferred")
        public static final String READ_PREFERENCE = "readPreference";

        @IntegerAnnotation(
                defaultValue = 100,
                comment = "connection per host")
        public static final String NUM_CONNECTIONS = "numConnections";

        @IntegerAnnotation(
                defaultValue = 5,
                comment = "max wait thread multiplier")
        public static final String MULTIPLIER = "multiplier";
    }

    public enum ReadPreferenceEnum {

        NEAREST("neast", ReadPreference.nearest()),
        PRIMARY("primary", ReadPreference.primary()),
        PRIMARYPREFERRED("primaryPreferred", ReadPreference.primaryPreferred()),
        SECONDARY("secondary", ReadPreference.secondary()),
        SECONDARYPREFERRED("secondaryPreferred",
            ReadPreference.secondaryPreferred());

        private final ReadPreference readPreference;

        private ReadPreferenceEnum(String name, ReadPreference readPreference) {
            this.readPreference = readPreference;
        }

        public ReadPreference getPreference() {
            return this.readPreference;
        }
    }

    @Override
    public MongoClient createObject(Object parameter) {
        ArrayList<ServerAddress> seeds = new ArrayList<ServerAddress>();
        for (String server : serverList) {
            String[] details = server.split(COLON);
            if (details.length != 2) {
                throw new IllegalArgumentException("Illegal Configuration: "
                        + "The list of mongo db servers parse fail");
            }
            try {
                ServerAddress serverAddress = new ServerAddress(details[0],
                        Integer.parseInt(details[1]));
                seeds.add(serverAddress);
            } catch (UnknownHostException ex) {
                throw new IllegalArgumentException("Illegal Configuration: "
                        + "The list of mongo db servers parse fail", ex);
            }
        }

        try {
            MongoClientOptions options = MongoClientOptions.builder().
                    readPreference(readPreferenceEnum.getPreference()).
                    connectionsPerHost(numConnections).
                    threadsAllowedToBlockForConnectionMultiplier(multiplier).
                    build();
            return new MongoClient(seeds, options);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Illegal Configuration : "
                    + "mongo client create failed", ex);
        }
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);

        serverList = (String[]) section.get(Meta.SERVERS_LIST);
        numConnections = (Integer) section.get(Meta.NUM_CONNECTIONS);
        multiplier = (Integer) section.get(Meta.MULTIPLIER);
        String readPreference = (String) section.get(Meta.READ_PREFERENCE);
        try {
            readPreferenceEnum =
                    ReadPreferenceEnum.valueOf(readPreference.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal Configuration : "
                    + "mongo client create failed", ex);
        }
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }
}
