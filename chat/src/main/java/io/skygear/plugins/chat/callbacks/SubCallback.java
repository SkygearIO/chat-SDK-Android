package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

public interface SubCallback<T> {
    void done(final String eventType, @Nullable T object);
}
