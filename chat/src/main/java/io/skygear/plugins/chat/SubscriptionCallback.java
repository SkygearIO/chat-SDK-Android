package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

public interface SubscriptionCallback {
    @NonNull
    String[] supportingEventTypes();

    void notify(final String eventType, @Nullable JSONObject data);

    void onSubscriptionFail(@Nullable String reason);
}
