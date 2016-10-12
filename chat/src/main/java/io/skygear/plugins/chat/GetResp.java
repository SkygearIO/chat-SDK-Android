package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;

/**
 * The Skygear Chat Plugin Object <b>Query</b> Response Handler.
 */
abstract class GetResp<T> extends RecordQueryResponseHandler {
    private final GetCallback<T> callback;

    /**
     * Default constructor of GetResp
     *
     * @param callback - the GetCallback<T> callback
     */
    GetResp(@Nullable GetCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * Convert Record to abstract type T
     *
     * @param records - query result records
     * @return the instance of abstract type T
     */
    @Nullable
    public abstract T onSuccess(Record[] records);

    /**
     * Query success callback.
     *
     * @param records - query result records
     */
    @Override
    public void onQuerySuccess(Record[] records) {
        if (callback != null) {
            callback.onSucc(onSuccess(records));
        }
    }

    /**
     * Query fail callback.
     *
     * @param reason the reason
     */
    @Override
    public void onQueryError(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
