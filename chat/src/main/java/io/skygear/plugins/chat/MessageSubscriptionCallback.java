package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The callback for message subscription.
 */
public abstract class MessageSubscriptionCallback implements SubscriptionCallback {
    static final String[] SUPPORTED_EVENT_TYPES = new String[]{
            "create",
            "update",
            "delete"
    };
    private static final String TAG = "SkygearChatSubscription";
    private final String conversationId;

    /**
     * Instantiates a new message subscription callback.
     *
     * @param conversation the conversation
     */
    public MessageSubscriptionCallback(@NonNull Conversation conversation) {
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
         *   "type": "record",
         *   "record_type": record_type,
         *   "record": {
         *     ...
         *   }
         * }
         **/

        if (data == null) {
            // nothing to do with null data
            return;
        }

        String dataType = data.optString("type");
        if (!"record".equals(dataType)) {
            Log.w(TAG, "Received non-record event. Ignore it.");
            return;
        }

        String recordType = data.optString("record_type");
        JSONObject recordData = data.optJSONObject("record");
        if (!"message".equals(recordType)) {
            // nothing to do if it is not a message record
            return;
        }

        Message message = null;
        if (recordData != null) {
            try {
                message = Message.fromJson(recordData);
            } catch (JSONException e) {
                Log.w(TAG, "Fail parsing message payload", e);
            }
        }

        if (message != null && message.getConversationId().equals(this.conversationId)) {
            this.notify(eventType, message);
        }
    }

    /**
     * Notify a message event.
     *
     * @param eventType the event type
     * @param message   the message
     */
    public abstract void notify(@NonNull String eventType, @NonNull Message message);
}
