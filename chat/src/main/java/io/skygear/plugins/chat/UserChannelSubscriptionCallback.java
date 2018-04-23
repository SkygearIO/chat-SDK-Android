package io.skygear.plugins.chat;


import android.support.annotation.NonNull;

/**
 * The callback for user channel subscription.
 */
public abstract class UserChannelSubscriptionCallback implements SubscriptionCallback {

    public static final String EVENT_TYPE_TYPING = "typing";
    public static final String EVENT_TYPE_CREATE = "create";
    public static final String EVENT_TYPE_UPDATE = "update";
    public static final String EVENT_TYPE_DELETE = "delete";

    static final String[] SUPPORTED_EVENT_TYPES = new String[]{
            EVENT_TYPE_TYPING,
            EVENT_TYPE_CREATE,
            EVENT_TYPE_UPDATE,
            EVENT_TYPE_DELETE
    };

    @NonNull
    @Override
    public String[] supportingEventTypes() {
        return this.SUPPORTED_EVENT_TYPES;
    }
}
