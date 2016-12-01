package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

/**
 * The callback interface for delete one object.
 */
public interface DeleteOneCallback {
    /**
     * Success callback.
     *
     * @param deletedId the deleted id
     */
    void onSucc(@Nullable String deletedId);

    /**
     * Fail callback.
     *
     * @param failReason the fail reason
     */
    void onFail(@Nullable String failReason);
}
