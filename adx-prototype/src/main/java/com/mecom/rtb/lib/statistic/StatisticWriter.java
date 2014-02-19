/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.statistic;

import com.adsame.algorithm.ctr.util.percentile.SizeBalancedTree;
import com.adsame.rtb.lib.configuration.Configuration;
import com.adsame.rtb.lib.configuration.Configuration.BooleanAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.LinkAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.LongAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.MetaAnnotation;
import com.adsame.rtb.lib.configuration.Configuration.Section;
import com.adsame.rtb.lib.configuration.Configuration.StringAnnotation;
import com.adsame.rtb.lib.configuration.Initializable;
import com.adsame.rtb.lib.configuration.Link;
import com.adsame.rtb.lib.util.FSTimer;
import com.adsame.rtb.lib.util.FSTimer.FSTask;
import com.adsame.rtb.lib.util.MysqlConnectionFactory;
import com.adsame.rtb.lib.util.ThreadLocalWrapper;
import com.adsame.rtb.lib.util.ThreadSafeHashMap;
import com.adsame.rtb.lib.util.TimeUtility;
import java.io.OutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticWriter implements Initializable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StatisticWriter.class);

    public static final int NANO_PER_MIllISECOND = 1000000;

    private static StatisticWriter statisticWriter;

    private static String adx;

    private ThreadSafeHashMap<String, SolutionStatistic> statisticHashMap =
            new ThreadSafeHashMap<String, SolutionStatistic>();
    private ThreadSafeHashMap<String, SizeBalancedTree<Long>> timePercentileHashMap =
            new ThreadSafeHashMap<String, SizeBalancedTree<Long>>();
    private static FSTimer fsTimer;

    private Section sectionLink;
    private Section section;

    private final int WRITE_ROWS_EVERY_EXCUTE = 1000;
    private String statisticTable;
    private boolean isWriteTimeMonitor;
    private String insertStatisticSql;
    ThreadLocalWrapper<Connection, Object> mysqlConnection;

    private MysqlConnectionFactory mysqlConnectionFactory;

    @MetaAnnotation(
            prefix = "statistic-writer",
            comment = "statistic writer configuration")
    public static final class Meta {

        @LinkAnnotation(
                comment = "database connection configuration file link")
        public static final String STATISTIC_DATABASE_CONFIG_FILE_LINK =
                "statisticDatabaseConfigFileLink";

        @LongAnnotation(
                comment = "time slot in millisecond",
                defaultValue = 600000)
        public static final String TIME_SLOT = "timeSlot";

        @StringAnnotation(
                comment = "statistic table")
        public static final String STATISTIC_TABLE = "statisticTable";

        @BooleanAnnotation(
                comment = "write time monitor info?",
                defaultValue = false)
        public static final String IS_WRITE_TIME_MONITOR = "isWriteTimeMonitor";
    }

    private StatisticWriter() {
    }

    private class StatisticWriterTask extends FSTask {

        StatisticWriterTask(FSTimer fsTimer) {
            fsTimer.super();
        }

        @Override
        public void runFSTask(long now) {
            try {
                long startTime = 0;
                long endTime = 0;
                if (LOGGER.isDebugEnabled()) {
                    startTime = System.currentTimeMillis();
                    LOGGER.debug("solution statistic timer start");
                }

                Map oldSolutionMap = statisticHashMap.getAndReplaceWithEmptyNew();
                Map oldPercentileMap = timePercentileHashMap.getAndReplaceWithEmptyNew();
                ComputerPercentile(oldPercentileMap, oldSolutionMap);
                Timestamp nowTimestamp = new Timestamp(now);
                updateStatistic(insertStatisticSql, oldSolutionMap, nowTimestamp);

                if (LOGGER.isDebugEnabled()) {
                    endTime = System.currentTimeMillis();
                    LOGGER.debug("solution statistic insert time {} ms",
                            endTime - startTime);
                }
            } catch (Exception ex) {
                LOGGER.error("solution statistic task exception!", ex);
            }
        }
    }

    public static synchronized void createStatisticWriter(Configuration configuration) {
        if (statisticWriter == null) {
            statisticWriter = new StatisticWriter();
            statisticWriter.initialize(configuration);
        }
    }

    public static StatisticWriter getStatisticWriter() {
        return statisticWriter;
    }

    public void clearStatisticWriter() {
        statisticHashMap.clear();
        timePercentileHashMap.clear();
    }

    public static void dispose() {
        fsTimer.cancel();
        statisticWriter = null;
    }

    @Override
    public void saveConfigurationRecursively(OutputStream outputStream,
            int level) throws IOException {
        sectionLink.save(outputStream, level);
        section.save(outputStream, level + 1);
        mysqlConnectionFactory.saveConfigurationRecursively(outputStream,
                level + 1);
    }

    @Override
    public void initialize(Configuration configuration) {
        sectionLink = configuration.getSection(Meta.class);

        Link statisticDatabaseConfigFileLink = (Link) sectionLink.get(
                Meta.STATISTIC_DATABASE_CONFIG_FILE_LINK);
        String statisticDatabaseConfigFilePath =
                statisticDatabaseConfigFileLink.getFullPath();
        Configuration statisticConfiguration = new Configuration();
        try {
            statisticConfiguration.load(statisticDatabaseConfigFilePath);
        } catch (IOException ex) {
            LOGGER.error("database connection config file {} load error",
                    statisticDatabaseConfigFilePath, ex);
            throw new IllegalArgumentException("Illegal Configuration");
        }

        section = statisticConfiguration.getSection(Meta.class);
        mysqlConnectionFactory = new MysqlConnectionFactory();
        mysqlConnectionFactory.initialize(statisticConfiguration);
        mysqlConnection = new ThreadLocalWrapper<Connection, Object>(
                mysqlConnectionFactory);

        long timeSlotMilliSecond = (Long) section.get(Meta.TIME_SLOT);
        fsTimer = new FSTimer(timeSlotMilliSecond);
        StatisticWriterTask task = new StatisticWriterTask(fsTimer);
        fsTimer.schedule(task);

        statisticTable = (String) section.get(Meta.STATISTIC_TABLE);
        insertStatisticSql = "insert into " + statisticTable +
                "(solutionID, adx, monitorField, monitorCount, averagePrice, time)" +
                "VALUES (?, ?, ?, ?, ?, ?);";

        isWriteTimeMonitor = (Boolean) section.get(Meta.IS_WRITE_TIME_MONITOR);
    }

    public static void setAdx(String adxName) {
        adx = adxName;
    }

    public void addSolutionMonitor(String solutionID,
            GeneralFieldEnum field) {
        try {
            statisticHashMap.getReadLock();
            getSolutionStatistic(solutionID, field.getValue()).
                    addFieldCountByOne();
        } finally {
            statisticHashMap.releaseReadLock();
        }
    }

    public void addSolutionMonitor(String solutionID,
            PriceFieldEnum priceField, double price) {
        try {
            statisticHashMap.getReadLock();
            String fieldValue = priceField.getValue();
            getSolutionStatistic(solutionID, fieldValue).
                    addFieldCountByOne();
            getSolutionStatistic(solutionID, fieldValue).
                    addTotalPrice(price);
        } finally {
            statisticHashMap.releaseReadLock();
        }
    }

    public void addSolutionMonitor(MonitorSize size) {
        try {
            statisticHashMap.getReadLock();
            getSolutionStatistic(size.statisticID, size.monitorField()).
                    addFieldCountByOne();
        } finally {
            statisticHashMap.releaseReadLock();
        }
    }

    public void addSolutionMonitor(MonitorTime time) {
        if (isWriteTimeMonitor == false) {
            return;
        }

        try {
            statisticHashMap.getReadLock();
            getSolutionStatistic(time.statisticID, time.enumMonitorTimeCount.getValue()).
                    addFieldCountByOne();
            getSolutionStatistic(time.statisticID, time.enumMonitorTime.getValue()).
                    addFieldCountByValue(time.time);

            synchronized(this) {
                TimePercentileEnum timePercentileEnum =
                    MonitorTime.getCorrespondTimePercentile(time.enumMonitorTime);
                MonitorPercentile timePercentile = new MonitorPercentile(
                    timePercentileEnum, time.time);
                getSortedList(timePercentile).add(timePercentile.time);
            }
        } finally {
            statisticHashMap.releaseReadLock();
        }
    }

    //only for google adx feedback
    public void addGoogleFeedBackMonitor(String solutionID, String feedBack) {
        try {
            statisticHashMap.getReadLock();
            getSolutionStatistic(solutionID, feedBack).addFieldCountByOne();
        } finally {
            statisticHashMap.releaseReadLock();
        }
    }

    private SolutionStatistic getSolutionStatistic(String solutionID,
            String fieldValue) {
        String key = adx + solutionID + fieldValue;
        return statisticHashMap.putValueIfAbsent(key,
                new SolutionStatistic(solutionID, fieldValue));
    }

    private synchronized SizeBalancedTree<Long> getSortedList(
            MonitorPercentile timePercentile) {
        String key = adx + MonitorPercentile.statisticID +
                timePercentile.enumPercentile.getValue();
        return timePercentileHashMap.putValueIfAbsent(key,
                new SizeBalancedTree<Long>());
    }

    private long getMonitorCountByField(String solutionID,
            String monitorField,
            Map<String, SolutionStatistic> SolutionHashMap) {
        String key = adx + solutionID + monitorField;
        SolutionStatistic solutionStatistic = SolutionHashMap.get(key);
        if (solutionStatistic == null) {
            return 0;
        }
        return solutionStatistic.monitorCount;
    }

    private void ComputerPercentile(
            Map<String, SizeBalancedTree<Long>> percentileHashMap,
            Map<String, SolutionStatistic> solutionHashMap) {
        Set set = percentileHashMap.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Entry<String, SizeBalancedTree<Long>>  entry =
                    (Entry<String, SizeBalancedTree<Long>>) it.next();
            SizeBalancedTree<Long> percentileSet = entry.getValue();
            for (int percentValue = 91; percentValue < 100; percentValue++) {
                float percent = (float) percentValue / 100;
                Long percentNumber = percentileSet.getPercentile(percent);
                SolutionStatistic solutionStatistic =
                        buildSolutionPercent(entry.getKey() + percentValue,
                                percentNumber);
                solutionHashMap.put(entry.getKey() + percentValue, solutionStatistic);
            }
        }
    }

    private SolutionStatistic buildSolutionPercent(String key, Long percentNumber) {
        String prefixKey = adx + MonitorPercentile.statisticID;
        String monitorField = key.replaceAll(prefixKey, "");
        SolutionStatistic solutionStatistic =
                new SolutionStatistic(MonitorPercentile.statisticID,
                monitorField, percentNumber/NANO_PER_MIllISECOND);
        return solutionStatistic;
    }

    private boolean updateStatistic(String sql, Map oldSolutionHashMap,
            Timestamp time) {
        Connection connection = mysqlConnection.getObject();
        if (connection == null) {
            LOGGER.error("get db connection error");
            return false;
        }

        PreparedStatement statement = null;
        Set set = oldSolutionHashMap.entrySet();
        Iterator it = set.iterator();
        int preparedCount = 0;
        try {
            statement = connection.prepareStatement(sql);
            while (it.hasNext()) {
                Entry<String, SolutionStatistic>  entry =
                        (Entry<String, SolutionStatistic>) it.next();
                SolutionStatistic solutionStatistic = entry.getValue();
                String monitorField = solutionStatistic.monitorField;
                long monitorCount = solutionStatistic.monitorCount;
                if (MonitorTime.isMonitorTimeField(monitorField)) {
                    long monitorTotalTime = monitorCount / NANO_PER_MIllISECOND;
                    long monitorTimeCount = getMonitorCountByField(
                            MonitorTime.statisticID,
                            MonitorTime.getCorrespondTimeCount(
                            MonitorTimeEnum.getValueOf(monitorField)).getValue(),
                            oldSolutionHashMap);
                    monitorCount = monitorTotalTime;
                    if (monitorTimeCount != 0) {
                        solutionStatistic.averageCount =
                                (float) monitorTotalTime / monitorTimeCount;
                        BigDecimal b = new BigDecimal(solutionStatistic.averageCount);
                        solutionStatistic.averageCount =
                                b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }
                statement.setString(1, solutionStatistic.solutionID);
                statement.setString(2, adx);
                statement.setString(3, monitorField);
                statement.setLong(4, monitorCount);
                statement.setDouble(5, solutionStatistic.averageCount);
                statement.setTimestamp(6, time);
                statement.addBatch();
                preparedCount++;
                if (preparedCount % WRITE_ROWS_EVERY_EXCUTE == 0) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
            }
            statement.executeBatch();
            statement.clearBatch();
        } catch (SQLException ex) {
            LOGGER.error("updateStatistic failed sql = {}", sql, ex);
            mysqlConnection.resetObject();
            return false;
        } finally {
            // The statement must be closed, otherwise it leaks
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                LOGGER.error("close statement error", ex);
            }
        }
        return true;
    }
}

