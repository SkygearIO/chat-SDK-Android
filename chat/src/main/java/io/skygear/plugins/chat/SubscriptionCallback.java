package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * The callback interface for Conversation Subscription.
 */
public interface SubscriptionCallback {
    /**
     * Supporting event types.
     *
     * @return the event types
     */
    @NonNull
    String[] supportingEventTypes();

    /**
     * Notify data event.
     *
     * @param eventType the event type
     * @param data      the data
     */
    void notify(final String eventType, @Nullable JSONObject data);

    /**
     * Subscription fail callback.
     *
     * @param reason the fail reason
     */
    void onSubscriptionFail(@Nullable String reason);
}
