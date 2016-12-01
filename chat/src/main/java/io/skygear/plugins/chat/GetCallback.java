package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

/**
 * The callback interface for getting objects.
 *
 * @param <T> the type parameter
 */
public interface GetCallback<T> {
    /**
     * Success callback
     *
     * @param object the object
     */
    void onSucc(@Nullable T object);

    /**
     * Fail callback
     *
     * @param failReason the fail reason
     */
    void onFail(@Nullable String failReason);
}
