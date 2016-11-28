package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

public abstract class TypingSubscriptionCallback implements SubscriptionCallback {
    static final String[] SUPPORTED_EVENT_TYPES = new String[]{
            "typing"
    };
    private final String conversationId;

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
        // TODO: 27/11/2016 Parse data into typing object
        /* Format will be:
         * {
         *       "conversation/1": {
         *           "user/1": {
         *               "event": "pause",
         *               "at": "20161116T78:44:00Z"
         *           }
         *       }
         *   }
         */

    }
}
