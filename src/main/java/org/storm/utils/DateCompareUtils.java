package org.storm.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by fm.chen on 2017/11/29.
 */
public final class DateCompareUtils {
    private DateCompareUtils() {}

    public static boolean diffLessEqual(Date minuend, Date subtrahend, long d, TimeUnit timeUnit) {
        long diff = minuend.getTime() - subtrahend.getTime();
        long diffUnit = timeUnit.convert(diff, TimeUnit.MILLISECONDS);
        return diffUnit <= d;
    }
}
