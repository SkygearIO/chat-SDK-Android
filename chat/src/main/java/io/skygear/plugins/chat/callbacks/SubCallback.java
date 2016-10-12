package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

/**
 * Subscription callback interface for Skygear Chat Plugin.
 */
public interface SubCallback<T> {
    /**
     * Callback when get notified.
     *
     * @param eventType - type of the event
     * @param object - the type T instance
     */
    void notify(final String eventType, @Nullable T object);
}
