/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync;

import com.adsame.algorithm.ctr.lib.data.Data;
import com.adsame.algorithm.ctr.lib.data.Schema;
import com.adsame.algorithm.ctr.lib.data.transformer.DataTransformer;
import com.adsame.algorithm.ctr.lib.data.transformer.MapTransformer;
import com.adsame.algorithm.ctr.lib.data.transformer.thrift.ThriftBasedMapTransformer;
import com.adsame.algorithm.ctr.lib.featuremanager.SchemaDataSet;
import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.dataset.client.DataSetClient;
import com.adsame.rtb.lib.dataset.client.impl.DatabaseDataSetClient;
import com.adsame.rtb.lib.util.ZlibUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecompressData {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecompressData.class);

    public static void main(String args[]) throws IOException {

        //String configPath = args[0];
        //LOGGER.info(
        //        "click notite server with config {}", configPath);
        String params = 
"database-data-set-client.host = 10.21.10.233\n" +
"database-data-set-client.port = 3306\n" +
"database-data-set-client.database = rtb_test\n" +
"database-data-set-client.username = root\n" +
"database-data-set-client.password = point9*\n" +
"database-data-set-client.tableNameList = feature_schema\n" +
"database-data-set-client.recordProcessorNameList = com.adsame.algorithm.ctr.lib.featuremanager.FeatureRecord$Processor\n" +
"database-data-set-client.dataSetProcessorName = com.adsame.algorithm.ctr.lib.featuremanager.SchemaDataSetProcessor\n";

        Configuration configuration = new Configuration();
       // configuration.load(configPath);
        configuration.loadFromString(params);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        configuration.save(outputStream);
        LOGGER.info("configuration:\n{}", outputStream.toString());

        DataSetClient schemaDataSetClient =
                (DataSetClient) configuration.createInstance(
                        DatabaseDataSetClient.class.getName());
        if (schemaDataSetClient == null) {
            throw new IllegalArgumentException("Illegal Configuration");
        }
        
        String ext = "eJxljk9qAjEcRjMBi9CNtiC1bsQDDMkvySRZWnQhKAMu3IZM_sAw1YgivYIHcuslvEqh6wa67Ld_732T5yvWqkBYyxmabuqP1Xpp5ovdcmtWCwMSbOUdU85WAaIr6AsaNLb1F7NPTfsZjD0esRYZFaA44TLyGBseCFfALURPPY3E-QqwZpn9J-R9lNf7Gd-xglc0dGlfhnOTUleegvXhhAW8odFfk2olGCOahkCkZRRLyM-V6qOny6E7pC8s";
        
        Data data = createDataFromString(ext, schemaDataSetClient);
        LOGGER.info("data {}", data.toString());
    }
    
     private static Data createDataFromString(String dataString, DataSetClient schemaDataSetClient) {
        if (dataString == null || dataString.isEmpty()) {
            return null;
        }
        byte compressBytes[] = Base64.decodeBase64(dataString);
        byte message[] = ZlibUtility.decompress(compressBytes);
        if (message == null) {
            return null;
        }
        return getDataTransformer(schemaDataSetClient).deserialize(message);
    }

    private static DataTransformer getDataTransformer(DataSetClient schemaDataSetClient) {
        SchemaDataSet schemaDataSet =
                (SchemaDataSet) schemaDataSetClient.retrieveDataSet();
        Schema schema = schemaDataSet.getSchema();

        MapTransformer mapTransformer = new ThriftBasedMapTransformer();
        return new DataTransformer(schema, mapTransformer);
    }
}
