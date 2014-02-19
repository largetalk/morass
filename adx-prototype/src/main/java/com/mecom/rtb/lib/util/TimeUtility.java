/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import com.adsame.rtb.lib.dataset.time.ClockTime;
import com.adsame.rtb.lib.dataset.time.DateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TimeUtility {

    public static final int MILLISECONDS_PER_SECOND;
    public static final int SECONDS_PER_MINUTE;
    public static final int MINUTES_PER_HOUR;
    public static final int HOURS_PER_DAY;
    public static final int DAYS_PER_WEEK;

    public static final long MILLISECONDS_PER_MINUTE;
    public static final long MILLISECONDS_PER_HOUR;
    public static final long MILLISECONDS_PER_DAY;
    public static final long MILLISECONDS_PER_WEEK;

    private static final HashMap<Integer, Integer> WEEKDAY_MAP;

    private static final long ORIGIN_TIME;

    private static final AtomicLong offset;

    static {
        MILLISECONDS_PER_SECOND = 1000;
        SECONDS_PER_MINUTE = 60;
        MINUTES_PER_HOUR = 60;
        HOURS_PER_DAY = 24;
        DAYS_PER_WEEK = 7;

        MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE;
        MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * MINUTES_PER_HOUR;
        MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * HOURS_PER_DAY;
        MILLISECONDS_PER_WEEK = MILLISECONDS_PER_DAY * DAYS_PER_WEEK;

        WEEKDAY_MAP = new HashMap<Integer, Integer>();
        int dayOfWeek = 0;
        WEEKDAY_MAP.put(Calendar.SUNDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.MONDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.TUESDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.WEDNESDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.THURSDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.FRIDAY, dayOfWeek++);
        WEEKDAY_MAP.put(Calendar.SATURDAY, dayOfWeek++);

        // Sun Dec 28 00:00:00 CST 1969
        ORIGIN_TIME = DateTime.valueOf("1969-12-28 00:00:00").getTime();

        offset = new AtomicLong(0);
    }

    public static class DayTimeSpan {

        public ClockTime start;
        public ClockTime end;

        public DayTimeSpan() {
            this.start = null;
            this.end = null;
        }

        public DayTimeSpan(ClockTime start, ClockTime end) {
            this.start = start;
            this.end = end;
        }

        public DayTimeSpan(String start, String end) {
            this.start = ClockTime.valueOf(start);
            this.end = ClockTime.valueOf(end);
        }

        public DayTimeSpan(long start, long end) {
            this.start = new ClockTime(start);
            this.end =  new ClockTime(end);
        }

        public long getInterval() {
            return end.getTime() - start.getTime();
        }

        public boolean isWithinTimeSpan(long time) {
            long timeToDayEnd = getIntervalToDayEnd(time);
            if (timeToDayEnd <= 0) {
                return false;
            }
            if (timeToDayEnd > getInterval()) {
                return false;
            }
            return true;
        }

        public long getIntervalToDayEnd(long time) {
            long timeFromDayBegin = time - getDayBasedFloor(time);
            long startTime = start.getTime();
            long startTimeFromDayBegin =
                    startTime - getDayBasedFloor(startTime);
            long endTimeFromDayBegin = startTimeFromDayBegin
                    + getInterval();
            return endTimeFromDayBegin - timeFromDayBegin;
        }
    }

    public static long getOffset() {
        return offset.get();
    }

    public static void setOffset(long offset) {
        TimeUtility.offset.set(offset);
    }

    public static long getTime() {
        return System.currentTimeMillis() + offset.get();
    }

    public static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTime());
        return calendar;
    }

    public static Calendar getCalendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    public static int getDayOfWeek() {
        Calendar calendar = getCalendar();
        return getDayOfWeek(calendar);
    }

    public static int getDayOfWeek(long time) {
        Calendar calendar = getCalendar(time);
        return getDayOfWeek(calendar);
    }

    public static int getDayOfWeek(Calendar calendar) {
        return WEEKDAY_MAP.get(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public static long getDayBasedCeil(long time) {
        long floor = getDayBasedFloor(time);
        if (floor == time) {
            return floor;
        } else {
            return floor + MILLISECONDS_PER_DAY;
        }
    }

    public static long getDayBasedFloor(long time) {
        Calendar calendar = TimeUtility.getCalendar(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getInterval(long startTime, long endTime,
            Integer weekday[]) {
        int intWeekday[] = new int[weekday.length];
        for (int i = 0; i < intWeekday.length; i++) {
            intWeekday[i] = weekday[i];
        }
        return getInterval(startTime, endTime, intWeekday);
    }

    public static long getInterval(long startTime, long endTime,
            int weekday[]) {
        DayTimeSpan wholeDay = new DayTimeSpan("00:00:00", "24:00:00");
        DayTimeSpan timeSpans[] = new DayTimeSpan[weekday.length];
        for (int i = 0; i < timeSpans.length; ++i) {
            timeSpans[i] = wholeDay;
        }
        return getInterval(startTime, endTime, weekday, timeSpans);
    }

    public static long getInterval(long startTime, long endTime,
            int weekday[], DayTimeSpan timeSpans[]) {
        if (endTime <= startTime || weekday.length != timeSpans.length) {
            return 0;
        }

        // this array indicates whether a specific day is included
        boolean includesDay[] = new boolean[DAYS_PER_WEEK];
        DayTimeSpan weekTimeSpans[] = new DayTimeSpan[DAYS_PER_WEEK];

        for (int i = 0; i < includesDay.length; i++) {
            includesDay[i] = false;
            weekTimeSpans[i] = null;
        }

        int numIncludedDaysPerWeek = 0;
        long millisecondsPerWeek = 0;
        for (int i = 0; i < weekday.length; i++) {
            int dayOfWeek = weekday[i];
            if (dayOfWeek >= 0 && dayOfWeek < DAYS_PER_WEEK
                    && !includesDay[dayOfWeek]) {
                includesDay[dayOfWeek] = true;
                weekTimeSpans[dayOfWeek] = timeSpans[i];
                numIncludedDaysPerWeek++;
                millisecondsPerWeek += timeSpans[i].getInterval();
            }
        }

        return getInterval(endTime, millisecondsPerWeek, includesDay,
                weekTimeSpans) - getInterval(startTime, millisecondsPerWeek,
                includesDay, weekTimeSpans);
    }

    private static long getInterval(long time, long millisecondsPerWeek,
            boolean includesDay[], DayTimeSpan includedDaySpanPairs[]) {
        int dayOfWeek = getDayOfWeek(time);

        long sum = 0;

        // handles whole weeks
        long numWeeks = (time - ORIGIN_TIME) / MILLISECONDS_PER_WEEK;
        sum += numWeeks * millisecondsPerWeek;

        // handles whole days
        for (int i = 0; i < dayOfWeek; i++) {
            if (includesDay[i]) {
                sum += includedDaySpanPairs[i].getInterval();
            }
        }

        // handles last day
        if (includesDay[dayOfWeek]) {
            DayTimeSpan daySpan = includedDaySpanPairs[dayOfWeek];
            long validTime;
            long dayInterval = daySpan.getInterval();
            long timeToDayEnd = daySpan.getIntervalToDayEnd(time);
            if (timeToDayEnd < 0) {
                validTime = dayInterval;
            } else if (timeToDayEnd > dayInterval) {
                validTime = 0;
            } else {
                validTime = dayInterval - timeToDayEnd;
            }
            sum += validTime;
        }
        return sum;
    }

    public static long getInterval(long startTime, long endTime,
            DayTimeSpan daysSpans[]) {
        if (daysSpans == null || daysSpans.length == 0
                || startTime >= endTime) {
            return 0;
        }
        long sum = 0;
        for (int i = 0; i < daysSpans.length; ++i) {
            long dayStart = daysSpans[i].start.getTime();
            long dayEnd = daysSpans[i].end.getTime();
            long start = dayStart < startTime ? startTime : dayStart;
            long end = dayEnd > endTime ? endTime : dayEnd;
            if (start < end) {
                sum += end - start;
            }
        }
        return sum;
    }

    public static int getIntervalOfDay(long time,
            long millisecondsPerInterval) {
        return (int) ((time - getDayBasedFloor(time))
                / millisecondsPerInterval);
    }

    public static int getIntervalGap(long intervalTime1, long intervalTime2,
            long millisecondsPerInterval) {
        return (int) ((intervalTime1 - intervalTime2)
                / millisecondsPerInterval);
    }

    public static boolean isDayBoundary(long time) {
        return time == getDayBasedFloor(time);
    }

    public static boolean isIntervalBoundary(long time,
            long millisecondsPerInterval){
        return (time - getDayBasedFloor(time)) % millisecondsPerInterval == 0;
    }

    public static long getIntervalBasedCeil(long time,
            long millisecondsPerInterval) {
        long floor = getIntervalBasedFloor(time, millisecondsPerInterval);
        if (floor == time) {
            return floor;
        } else {
            return floor + millisecondsPerInterval;
        }
    }

    public static long getIntervalBasedFloor(long time,
            long millisecondsPerInterval) {
        long floor = getDayBasedFloor(time);
        return time - (time - floor) % millisecondsPerInterval;
    }

    public static long getWeekBasedFloor(long time) {
        Calendar calendar = TimeUtility.getCalendar(time);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
