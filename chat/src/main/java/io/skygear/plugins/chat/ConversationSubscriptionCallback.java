package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The callback for conversation subscription.
 */
public abstract class ConversationSubscriptionCallback implements SubscriptionCallback {
    public static final String EVENT_TYPE_CREATE = "create";
    public static final String EVENT_TYPE_UPDATE = "update";
    public static final String EVENT_TYPE_DELETE = "delete";

    static final String[] SUPPORTED_EVENT_TYPES = new String[]{
            EVENT_TYPE_CREATE,
            EVENT_TYPE_UPDATE,
            EVENT_TYPE_DELETE
    };

    private static final String RECORD_TYPE = "conversation";

    private static final String TAG = "ConversationSubscribe";
    static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();

    @NonNull
    @Override
    public String[] supportingEventTypes() {
        return this.SUPPORTED_EVENT_TYPES;
    }

    @Override
    public void notify(String eventType, @Nullable JSONObject data) {
        String recordType = data.optString("record_type");
        if (RECORD_TYPE.equals(recordType)) {
            JSONObject object =data.optJSONObject("record");
            if (object != null) {
                try {
                    Conversation conversation = Conversation.fromJson(object);
                    this.notify(eventType, conversation);
                } catch (JSONException e) {
                    Log.w(TAG, "Cannot parse conversation.", e);
                }
            } else {
                Log.w(TAG, "Record not found.");
            }
        } else {
            Log.w(TAG, String.format("record_type is %s, not %s", recordType, RECORD_TYPE) );
        }
    }

    /**
     * Notify a conversation event.
     *
     * @param eventType the event type
     * @param conversation   the message
     */
    public abstract void notify(@NonNull String eventType, @NonNull Conversation conversation);
}
