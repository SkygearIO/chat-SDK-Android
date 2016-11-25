package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

public interface SubCallback<T> {
    void notify(final String eventType, @Nullable T object);
}
