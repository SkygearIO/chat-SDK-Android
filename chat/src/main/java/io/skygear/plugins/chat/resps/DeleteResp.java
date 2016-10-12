package io.skygear.plugins.chat.resps;


import java.util.Map;

import io.skygear.plugins.chat.callbacks.DeleteOneCallback;
import io.skygear.skygear.RecordDeleteResponseHandler;

public final class DeleteResp extends RecordDeleteResponseHandler {
    private DeleteOneCallback callback;

    public DeleteResp(DeleteOneCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDeleteSuccess(String[] ids) {
        if (callback != null) {
            callback.onSucc(ids[0]);
        }
    }

    @Override
    public void onDeletePartialSuccess(String[] ids, Map<String, String> reasons) {

    }

    @Override
    public void onDeleteFail(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
