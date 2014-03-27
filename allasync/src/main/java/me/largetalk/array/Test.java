package me.largetalk.array;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by largetalk on 3/12/14.
 */
public class Test {

    public static void main(String args[]) {
        ArrayList<String> lst = new ArrayList<String>();
        lst.add("abc");
        lst.add("123");
        lst.add("uvw");
        String b[] = lst.toArray(new String[2]);
        System.out.println(b.length);
        System.out.println(b[0]);
        long time = System.currentTimeMillis();
        System.out.println(time);
        System.out.println(getDayBasedFloor(time));
    }
    
    public static long getDayBasedFloor(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
}
