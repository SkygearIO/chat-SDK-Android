package io.skygear.plugins.chat;

import io.skygear.skygear.Error;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The callback interface for saving object
 *
 * @param <T> the type parameter
 */
public interface SaveCallback<T> {
    /**
     * Success callback
     *
     * @param object the object
     */
    void onSucc(@Nullable T object);

    /**
     * Fail callback
     *
     * @param error the fail reason
     */
    void onFail(@NonNull Error error);
}
