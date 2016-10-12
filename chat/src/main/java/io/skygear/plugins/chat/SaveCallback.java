package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

/**
 * Save callback interface for Skygear Chat Plugin.
 */
public interface SaveCallback<T> {
    /**
     * Callback when save successfully.
     *
     * @param object - the type T instance
     */
    void onSucc(@Nullable T object);

    /**
     * Callback when save failed.
     *
     * @param failReason - the reason
     */
    void onFail(@Nullable String failReason);
}
