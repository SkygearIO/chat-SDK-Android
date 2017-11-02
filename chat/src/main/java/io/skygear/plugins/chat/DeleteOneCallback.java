package io.skygear.plugins.chat;

import io.skygear.skygear.Error;
import android.support.annotation.NonNull;
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
     * @param error the fail reason
     */
    void onFail(@NonNull Error error);
}
