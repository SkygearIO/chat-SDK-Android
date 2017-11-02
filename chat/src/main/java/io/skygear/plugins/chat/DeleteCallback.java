package io.skygear.plugins.chat;

import io.skygear.skygear.Error;
import android.support.annotation.NonNull;

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
     * @param error the fail reason
     */
    void onFail(@NonNull Error error);
}
