package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.skygear.Record;
import io.skygear.skygear.RecordSaveResponseHandler;

abstract class SaveResponseAdapter<T> extends RecordSaveResponseHandler {
    private final SaveCallback<T> callback;

    SaveResponseAdapter(@Nullable final SaveCallback<T> callback) {
        this.callback = callback;
    }

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
                                       Map<String, String> reasons) {

    }

    @Override
    public void onSaveFail(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
