package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.skygear.skygear.PubsubContainer;
import io.skygear.skygear.PubsubHandler;

/**
 * The Conversation Subscription.
 */
final class Subscription {
    private static final String TAG = "SkygearChatSubscription";

    private final String conversationId;
    private final String channel;
    private final PubsubHandler handler;
    private final Map<String, SubscriptionCallback> callbackMap;

    /**
     * Instantiates a new Conversation Subscription.
     *
     * @param conversationId the conversation id
     * @param channel        the channel
     * @param callback       the callback
     */
    Subscription(@NonNull final String conversationId,
                 @NonNull final String channel,
                 @Nullable final SubscriptionCallback callback) {
        this.conversationId = conversationId;
        this.channel = channel;
        this.callbackMap = new HashMap<>();

        this.addCallBack(callback);
        this.handler = new PubsubHandler() {
            @Override
            public void handle(JSONObject data) {
                if (data != null) {
                    Subscription.this.handleEvent(data);
                }
            }
        };
    }

    /**
     * Add callback.
     *
     * @param callback the callback
     */
    void addCallBack(@Nullable SubscriptionCallback callback) {
        if (callback == null) {
            // nothing to do
            return;
        }

        String[] eventTypes = callback.supportingEventTypes();
        for (String eachEventType : eventTypes) {
            this.callbackMap.put(eachEventType, callback);
        }
    }

    /**
     * Attach to a Skygear Pubsub.
     *
     * @param pubsub the pubsub
     */
    void attach(final PubsubContainer pubsub) {
        pubsub.subscribe(channel, this.handler);
    }

    /**
     * Detach from a Skygear Pubsub.
     *
     * @param pubsub the pubsub
     */
    void detach(final PubsubContainer pubsub) {
        pubsub.unsubscribe(channel, this.handler);
    }

    /**
     * Handle event.
     *
     * @param data the data
     */
    void handleEvent(final JSONObject data) {
        String eventName = data.optString("event");
        if (eventName != null) {
            JSONObject eventData = data.optJSONObject("data");
            SubscriptionCallback callback = this.callbackMap.get(eventName);
            if (callback != null) {
                callback.notify(eventName, eventData);
            }
        }
    }
}
