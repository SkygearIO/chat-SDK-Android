package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

public interface DeleteOneCallback {
    void onSucc(@Nullable String deletedId);

    void onFail(@Nullable String failReason);
}
