    /*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync;

import com.adsame.util.LDB;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupLDB {

    private static Logger logger = LoggerFactory.getLogger(BackupLDB.class.getName());

    public static void main(String args[]) {
        String prefix = "/home/matcher/";
        String backPrefix = "/home/matcher/backup20150525/";
        String adxs[] = new String[]{"baidu", "google", "miaozhen", "taobao", "tencent"};
        for (int i = 0; i < adxs.length; i++) {
            try {
                String adx = adxs[i];
                LDB dbAdxAdsame = new LDB();
                LDB dbAdsameAdx = new LDB();
                LDB backUpdbAdxAdsame = new LDB();
                LDB backUpdbAdsameAdx = new LDB();

                dbAdxAdsame.setup(prefix + "adxadsame", adx, 100, 100, 600);
                dbAdsameAdx.setup(prefix + "adsameadx", adx, 100, 100, 600);

                backUpdbAdxAdsame.setup(backPrefix + "adxadsame", adx, 100, 100, 600);
                backUpdbAdsameAdx.setup(backPrefix + "adsameadx", adx, 100, 100, 600);

                load(dbAdxAdsame, backUpdbAdxAdsame);
                load(dbAdsameAdx, backUpdbAdsameAdx);
            } catch (Exception e) {
                logger.error("exception", e);
            }
        }

    }

    public static void load(LDB oldDB, LDB newDB) {
        if (oldDB.database == null) {
            return;
        }
        try {
            long waste = System.currentTimeMillis();
            DBIterator it = oldDB.database.iterator();
            byte[] keys;
            byte[] values;
            long count = 0;
            for (it.seekToFirst(); it.hasNext(); it.next()) {
                keys = it.peekNext().getKey();
                values = it.peekNext().getValue();
                newDB.put(keys, values);
                count++;
                if (count % 1000 == 0) {
                    System.out.println("count:" + count +", time: " + (System.currentTimeMillis() - waste));
                }
            }
        } catch (Exception e) {
            logger.error("exception", e);
        } finally {
            oldDB.close();
            newDB.close();
        }
    }
}
