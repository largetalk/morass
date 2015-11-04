/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.spark;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class SparkTest {

    public static void main(String args[]) throws InterruptedException {
         SparkConf conf = new SparkConf();
         conf.setAppName("spark_local_test");
         conf.setMaster("local");
         conf.set("spark.cassandra.connection.host", "10.21.10.119,10.21.10.233");
         conf.set("spark.cassandra.input.split.size_in_mb", "102400");
         conf.set("spark.driver.maxResultSize", "0");
         //conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
         conf.set("spark.driver.allowMultipleContexts", "true");
         
         SparkContext sparkContext = new SparkContext(conf);
         JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
         String filePath = "/home/adsame/git/samedatabackend/java/samedata-lib/test-data/client/spark.properties";
         JavaRDD<String> lines = javaSparkContext.textFile(filePath);
         JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {

             @Override
             public Iterable<String> call(String t) throws Exception {
                 return Arrays.asList(t.split("="));
             }
         });
         
         JavaPairRDD<String, Integer> ones = words.mapToPair(
                 new PairFunction<String, String, Integer>() {

             @Override
             public Tuple2<String, Integer> call(String t) throws Exception {
                 return new Tuple2<String, Integer>(t, 1);
             }
         });
         
         JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {

             @Override
             public Integer call(Integer t1, Integer t2) throws Exception {
                 return t1 + t2;
             }
         });
         
          List<Tuple2<String, Integer>> output = counts.collect();
          System.out.println(output);
          
          for (Tuple2<String, Integer> e : output) {
              System.out.println(e._1 + " : " + e._2);
          }
    }
}
