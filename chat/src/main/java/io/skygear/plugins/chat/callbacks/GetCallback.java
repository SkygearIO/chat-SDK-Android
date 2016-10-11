package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

public interface GetCallback<T> {
    void onSucc(@Nullable T object);
    void onFail(@Nullable String failReason);
}
