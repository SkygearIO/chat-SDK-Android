package io.skygear.plugins.chat.utils;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

public final class DateUtils {
    private DateUtils() {

    }

    public static String toISO8601(final Date date) {
        return new DateTime(date, DateTimeZone.UTC).toString();
    }
}
