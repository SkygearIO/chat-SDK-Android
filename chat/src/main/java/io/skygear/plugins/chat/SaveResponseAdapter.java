package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.skygear.Error;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordSaveResponseHandler;

/**
 * An adapter converting record save response to save object callback
 *
 * @param <T> the type parameter
 */
abstract class SaveResponseAdapter<T> extends RecordSaveResponseHandler {
    private final SaveCallback<T> callback;

    /**
     * Instantiates a new save response adapter.
     *
     * @param callback the callback
     */
    SaveResponseAdapter(@Nullable final SaveCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * The convert method.
     *
     * @param record the record
     * @return the type parameter
     */
    @Nullable
    public abstract T convert(Record record);

    @Override
    public void onSaveSuccess(Record[] records) {
        Record record = records[0];
        if (callback != null) {
            callback.onSucc(convert(record));
        }
    }

    @Override
    public void onPartiallySaveSuccess(Map<String, Record> successRecords,
                                       Map<String, Error> reasons) {

    }

    @Override
    public void onSaveFail(Error reason) {
        if (callback != null) {
            callback.onFail(reason.getMessage());
        }
    }
}
