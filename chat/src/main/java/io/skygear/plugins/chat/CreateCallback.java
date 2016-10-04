package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

public interface CreateCallback<T> {
    void done(@Nullable T object, @Nullable String failReason);
}
