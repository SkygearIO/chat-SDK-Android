package io.skygear.plugins.chat.utils;


import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * The Skygear Chat Plugin - Utils for Date class.
 */
public final class DateUtils {
    private DateUtils() {

    }

    /**
     * Convert Date instance to ISO 8601 String.
     *
     * @param date - input Date instance
     * @return ISO 8601 String of the Date instance
     */
    public static String toISO8601(@NonNull final Date date) {
        return new DateTime(date, DateTimeZone.UTC).toString();
    }
}
