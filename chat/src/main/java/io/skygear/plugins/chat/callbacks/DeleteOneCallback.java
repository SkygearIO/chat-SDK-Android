package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

/**
 * Delete one record callback interface for Skygear Chat Plugin.
 */
public interface DeleteOneCallback {
    /**
     * Callback when delete successfully.
     *
     * @param deletedId - the id of the deleted record
     */
    void onSucc(@Nullable String deletedId);

    /**
     * Callback when delete failed.
     *
     * @param failReason - the reason
     */
    void onFail(@Nullable String failReason);
}
