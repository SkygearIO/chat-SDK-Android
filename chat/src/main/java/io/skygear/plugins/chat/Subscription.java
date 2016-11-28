package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.skygear.skygear.Pubsub;

final class Subscription {
    private static final String TAG = "SkygearChatSubscription";

    private final String conversationId;
    private final String channel;
    private final Pubsub.Handler handler;
    private final Map<String, SubscriptionCallback> callbackMap;

    Subscription(@NonNull final String conversationId,
                 @NonNull final String channel,
                 @Nullable final SubscriptionCallback callback) {
        this.conversationId = conversationId;
        this.channel = channel;
        this.callbackMap = new HashMap<>();

        this.addCallBack(callback);
        this.handler = new Pubsub.Handler() {
            @Override
            public void handle(JSONObject data) {
                if (data != null) {
                    Subscription.this.handleEvent(data);
                }
            }
        };
    }

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

    void attach(final Pubsub pubsub) {
        pubsub.subscribe(channel, this.handler);
    }

    void detach(final Pubsub pubsub) {
        pubsub.unsubscribe(channel, this.handler);
    }

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
