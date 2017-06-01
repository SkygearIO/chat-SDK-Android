package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

/**
 * The callback interface for deleting objects.
 *
 * @param <T> the type parameter
 */
public interface DeleteCallback<T> {
    /**
     * Success callback
     *
     * @param object the object
     */
    void onSucc(T object);

    /**
     * Fail callback
     *
     * @param failReason the fail reason
     */
    void onFail(@Nullable String failReason);
}
