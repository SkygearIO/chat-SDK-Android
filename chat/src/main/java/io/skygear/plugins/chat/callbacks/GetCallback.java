package io.skygear.plugins.chat.callbacks;


import android.support.annotation.Nullable;

/**
 * Get callback interface for Skygear Chat Plugin.
 */
public interface GetCallback<T> {
    /**
     * Callback when get successfully.
     *
     * @param object - the type T instance
     */
    void onSucc(@Nullable T object);

    /**
     * Callback when get failed.
     *
     * @param failReason - the reason
     */
    void onFail(@Nullable String failReason);
}
