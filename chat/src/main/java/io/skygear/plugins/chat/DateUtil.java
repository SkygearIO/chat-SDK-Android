package io.skygear.plugins.chat;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtil {
    private DateUtil() {

    }

    public static String toISO8601(final Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}
