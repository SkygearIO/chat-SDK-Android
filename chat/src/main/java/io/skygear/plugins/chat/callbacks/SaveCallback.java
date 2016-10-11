package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

public interface SaveCallback<T> {
    void onSucc(@Nullable T object);
    void onFail(@Nullable String failReason);
}
