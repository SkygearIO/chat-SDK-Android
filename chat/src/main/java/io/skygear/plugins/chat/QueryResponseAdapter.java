package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import io.skygear.skygear.Error;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;

/**
 * An Adapter converting Query Response to get object callback.
 *
 * @param <T> the type parameter
 */
abstract class QueryResponseAdapter<T> extends RecordQueryResponseHandler {
    private final GetCallback<T> callback;

    /**
     * Instantiates a new query response adapter.
     *
     * @param callback the callback
     */
    QueryResponseAdapter(@Nullable GetCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * The convert method.
     *
     * @param records the records
     * @return the type parameter
     */
    @Nullable
    public abstract T convert(Record[] records);

    @Override
    public void onQuerySuccess(Record[] records) {
        if (callback != null) {
            callback.onSucc(convert(records));
        }
    }

    @Override
    public void onQueryError(Error error) {
        if (callback != null) {
            callback.onFail(error);
        }
    }
}
