package io.skygear.plugins.chat;


import io.skygear.skygear.Error;
import android.support.annotation.NonNull;
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
    void onSuccess(@Nullable T object);

    /**
     * Fail callback
     *
     * @param error the fail reason
     */
    void onFail(@NonNull Error error);
}
