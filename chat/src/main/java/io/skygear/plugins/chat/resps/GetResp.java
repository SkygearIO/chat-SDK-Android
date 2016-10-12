package io.skygear.plugins.chat.resps;


import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;

public abstract class GetResp<T> extends RecordQueryResponseHandler {
    private GetCallback<T> callback;

    public GetResp(GetCallback<T> callback) {
        this.callback = callback;
    }

    public abstract T onSuccess(Record[] records);

    @Override
    public void onQuerySuccess(Record[] records) {
        if (callback != null) {
            callback.onSucc(onSuccess(records));
        }
    }

    @Override
    public void onQueryError(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
