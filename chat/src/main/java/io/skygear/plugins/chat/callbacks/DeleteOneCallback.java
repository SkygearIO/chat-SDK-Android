package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

public interface DeleteOneCallback {
    void onSucc(@Nullable String deletedId);
    void onFail(@Nullable String failReason);
}
