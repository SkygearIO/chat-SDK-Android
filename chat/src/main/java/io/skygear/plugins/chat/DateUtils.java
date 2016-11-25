package io.skygear.plugins.chat;


import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

final class DateUtils {
    private DateUtils() {

    }

    static String toISO8601(@NonNull final Date date) {
        return new DateTime(date, DateTimeZone.UTC).toString();
    }
}
