package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;

abstract class QueryResponseAdapter<T> extends RecordQueryResponseHandler {
    private final GetCallback<T> callback;

    QueryResponseAdapter(@Nullable GetCallback<T> callback) {
        this.callback = callback;
    }

    @Nullable
    public abstract T convert(Record[] records);

    @Override
    public void onQuerySuccess(Record[] records) {
        if (callback != null) {
            callback.onSucc(convert(records));
        }
    }

    @Override
    public void onQueryError(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
