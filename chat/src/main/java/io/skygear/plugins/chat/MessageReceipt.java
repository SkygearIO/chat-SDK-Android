package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MessageReceipt {
    static final String USER_ID_KEY = "user_id";
    static final String READ_AT_KEY = "read_at";
    static final String DELIVERED_AT_KEY = "delivered_at";

    private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();

    private final String userId;
    private final Date readAt;
    private final Date deliveredAt;

    MessageReceipt(@NonNull String userId, @Nullable Date readAt, @Nullable Date deliveredAt) {
        super();

        this.userId = userId;
        this.readAt = readAt;
        this.deliveredAt = deliveredAt;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @Nullable
    public Date getReadAt() {
        return readAt;
    }

    @Nullable
    public Date getDeliveredAt() {
        return deliveredAt;
    }

    static MessageReceipt fromJSON(@NonNull JSONObject jsonObject) throws JSONException {
        String userId = jsonObject.getString(USER_ID_KEY);

        Date readAt = null;
        if (jsonObject.has(READ_AT_KEY)) {
            String readAtString = jsonObject.getString(READ_AT_KEY);
            readAt = MessageReceipt.dateTimeFormatter.parseDateTime(readAtString).toDate();
        }

        Date deliveredAt = null;
        if (jsonObject.has(DELIVERED_AT_KEY)) {
            String deliveredAtString = jsonObject.getString(DELIVERED_AT_KEY);
            deliveredAt = MessageReceipt.dateTimeFormatter.parseDateTime(deliveredAtString).toDate();
        }

        return new MessageReceipt(userId, readAt, deliveredAt);
    }
}
