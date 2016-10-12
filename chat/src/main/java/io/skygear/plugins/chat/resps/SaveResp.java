package io.skygear.plugins.chat.resps;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.plugins.chat.callbacks.SaveCallback;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordSaveResponseHandler;

/**
 * The Skygear Chat Plugin Object <b>Save</b> Response Handler.
 */
public abstract class SaveResp<T> extends RecordSaveResponseHandler {
    private final SaveCallback<T> callback;

    /**
     * Default constructor of SaveResp
     *
     * @param callback - the SaveCallback<T> callback
     */
    public SaveResp(@Nullable final SaveCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * Convert Record to abstract type T
     *
     * @param record - save result record
     * @return the instance of abstract type T
     */
    @Nullable
    public abstract T onSuccess(Record record);

    /**
     * Save success callback.
     *
     * @param records - save result records
     */
    @Override
    public void onSaveSuccess(Record[] records) {
        Record record = records[0];
        if (callback != null) {
            callback.onSucc(onSuccess(record));
        }
    }

    @Override
    public void onPartiallySaveSuccess(Map<String, Record> successRecords,
                                       Map<String, String> reasons) {

    }

    /**
     * Save fail callback.
     *
     * @param reason the reason
     */
    @Override
    public void onSaveFail(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
