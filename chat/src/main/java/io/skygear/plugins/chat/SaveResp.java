package io.skygear.plugins.chat;


import java.util.Map;

import io.skygear.plugins.chat.callbacks.SaveCallback;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordSaveResponseHandler;

public abstract class SaveResp<T> extends RecordSaveResponseHandler {
    private SaveCallback<T> callback;

    public SaveResp(final SaveCallback<T> callback) {
        this.callback = callback;
    }

    public abstract T onSuccess(Record record);

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

    @Override
    public void onSaveFail(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
