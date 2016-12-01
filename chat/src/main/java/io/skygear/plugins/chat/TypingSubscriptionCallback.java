package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The callback for typing subscription.
 */
public abstract class TypingSubscriptionCallback implements SubscriptionCallback {
    /**
     * The Supported event types.
     */
    static final String[] SUPPORTED_EVENT_TYPES = new String[]{
            "typing"
    };
    private static final String TAG = "SkygearChatSubscription";
    static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
    private final String conversationId;

    /**
     * Instantiates a new typing subscription callback.
     *
     * @param conversation the conversation
     */
    public TypingSubscriptionCallback(@NonNull Conversation conversation) {
        super();

        this.conversationId = conversation.getId();
    }

    @NonNull
    @Override
    public String[] supportingEventTypes() {
        return this.SUPPORTED_EVENT_TYPES;
    }

    @Override
    public void notify(String eventType, @Nullable JSONObject data) {
        /**
         *  Format will be:
         * {
         *     "conversation/1": {
         *         "user/1": {
         *             "event": "pause",
         *             "at": "20161116T78:44:00Z"
         *         }
         *     }
         * }
         */

        if (data == null) {
            // nothing to do with null data
            return;
        }

        Iterator<String> dataKeys = data.keys();
        JSONObject typingData = data.optJSONObject("conversation/" + this.conversationId);
        if (typingData == null) {
            // nothing to do if no typing data for the desired conversation
            return;
        }

        List<Typing> typingList = new LinkedList<>();
        Iterator<String> typingDataKeys = typingData.keys();
        while (typingDataKeys.hasNext()) {
            String eachTypingUserId = typingDataKeys.next();
            String[] userIdSplits = eachTypingUserId.split("/", 2);

            if (userIdSplits.length < 2 || !userIdSplits[0].equalsIgnoreCase("user")) {
                Log.w(TAG, "Invalid format for user ID for typing event");
                continue;
            }

            try {
                JSONObject userTypingData = typingData.getJSONObject(eachTypingUserId);
                String eventName = userTypingData.getString("event");
                String eventTimeString = userTypingData.optString("at");
                Date eventTime = null;

                if (eventTimeString != null) {
                    eventTime = TypingSubscriptionCallback.dateTimeFormatter
                            .parseDateTime(eventTimeString).toDate();
                }

                typingList.add(new Typing(
                        userIdSplits[1],
                        Typing.State.fromName(eventName),
                        eventTime
                ));
            } catch (JSONException e) {
                Log.w(TAG, "Fail to parse typing event payload", e);
            }
        }

        if (typingList.size() > 0) {
            this.notify(typingList);
        }
    }

    /**
     * Notify a typing event.
     *
     * @param typingList the typing list
     */
    public abstract void notify(@NonNull List<Typing> typingList);
}
