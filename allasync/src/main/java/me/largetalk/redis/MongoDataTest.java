/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.redis;

import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.LinkAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.ConfigurationUtility;
import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.configuration.Link;
import com.adsame.rtb.lib.util.MongoDBPartitionManager;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDataTest implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            MongoDataTest.class);

    private Section section;
    private MongoDBPartitionManager mongoDBMgr;
    private String collectionName;
    private String userIDFieldName;
    private File idFile;

    @MetaAnnotation(
            prefix = "mongo-test",
            comment = "mongo test configuration")
    public static final class Meta {

        @LinkAnnotation(
                comment = "write id file")
        public static final String ID_FILE = "idFile";

        @StringAnnotation(
                comment = "name of user group impression list collection")
        public static final String COLLECTION_NAME = "collectionName";

        @StringAnnotation(
                comment = "field name of user id")
        public static final String USER_ID_FIELD_NAME =
                "userIDFieldName";
    }

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            String className = MongoDataTest.class.getName();
            System.err.printf("Usage: %s config log4j.properties", className);
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                LOGGER.info("shut down signal captured");
                LogManager.shutdown();
            }
        });

        String configPath = args[0];
        String log4jPropertiesPath = args[1];

        PropertyConfigurator.configureAndWatch(log4jPropertiesPath, 10000);

        LOGGER.info("frequency capper with config {} and log4j properties {}",
                configPath, log4jPropertiesPath);

        Configuration configuration = new Configuration();
        configuration.load(configPath);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        configuration.save(outputStream);
        LOGGER.info("configuration:\n{}", outputStream.toString());

        MongoDataTest server =
                new MongoDataTest();
        try {
            server.initialize(configuration);
        } catch (Exception ex) {
            LOGGER.error("server initialize failed", ex);
            server = null;
        }

        if (server != null) {
            server.run();
        }
    }

    @Override
    public void initialize(Configuration configuration) {
        section = configuration.getSection(Meta.class);
        String mongoDBPartitionManagerName =
                MongoDBPartitionManager.class.getName();
        mongoDBMgr =
                (MongoDBPartitionManager) configuration.createInstance(
                        mongoDBPartitionManagerName);
        if (mongoDBMgr == null) {
            ConfigurationUtility.logAndThrow(
                    "mongo db partition manager create failed");
        }
        collectionName = (String) section.get(Meta.COLLECTION_NAME);
        mongoDBMgr.setCollectionName(collectionName);

        Link idLink = (Link) section.get(Meta.ID_FILE);
        idFile = new File(idLink.getFullPath());

        userIDFieldName = (String) section.get(Meta.USER_ID_FIELD_NAME);
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        section.save(outputStream, level);
    }

    public void run() throws IOException {
        final List<String> idList = FileUtils.readLines(idFile);

        long s1 = System.currentTimeMillis();
        for (int i = 0; i < idList.size(); i++) {
            String ids[] = idList.get(i).split(" ");
            if (ids.length != 3) {
                continue;
            }
            String userID = ids[1];
            if (userID.equals("null")) {
                userID = ids[0];
            }

            long solution = Long.parseLong(ids[2]);
            Document o = fullGet(userID);
            if (i % 10000 == 0) {
                LOGGER.info("full get {}", i);
            }
        }
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        for (int i = 0; i < idList.size(); i++) {
            String ids[] = idList.get(i).split(" ");
            if (ids.length != 3) {
                continue;
            }
            String userID = ids[1];
            if (userID.equals("null")) {
                userID = ids[0];
            }

            long solution = Long.parseLong(ids[2]);
            Document o = partGet(userID, solution);
            if (i % 10000 == 0) {
                LOGGER.info("part get {}", i);
            }
        }
        long e2 = System.currentTimeMillis();

        LOGGER.info("all {}, projection {}", e1 - s1, e2 - s2);
    }

    private Document fullGet(String userid) {
        MongoCollection<Document> col = getDBCollectionByCookie(userid);
        return col.find(eq(userIDFieldName, userid)).first();
    }

    private Document partGet(String userid, long solution) {
        MongoCollection<Document> col = getDBCollectionByCookie(userid);
        List<Long> gids = new ArrayList<Long>();
        gids.add(solution);
        return col.find(eq(userIDFieldName, userid))
                .projection(buildUserGroupProjection(gids)).first();
    }

    private MongoCollection<Document> getDBCollectionByCookie(String cookie) {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(23, 13);
        hashCodeBuilder.append(cookie);
        int blockID = Math.abs(hashCodeBuilder.toHashCode());
        return mongoDBMgr.getDBCollection(blockID);
    }

    private Bson buildUserGroupProjection(List<Long> groupIDs) {
        List<String> projections = new ArrayList<String>();
        for (Long groupID : groupIDs) {
            projections.add(groupID.toString());
        }
        return include(projections.toArray(new String[0]));
    }
}
