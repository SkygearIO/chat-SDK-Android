package io.skygear.plugins.chat;


import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * The Date Utils.
 */
final class DateUtils {
    /**
     * Convert a date object to string in ISO 8601 format
     *
     * @param date the date
     * @return the string
     */
    static String toISO8601(@NonNull final Date date) {
        return new DateTime(date, DateTimeZone.UTC).toString();
    }
}
