package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.skygear.RecordDeleteResponseHandler;

final class DeleteResponseAdapter extends RecordDeleteResponseHandler {
    private final DeleteOneCallback callback;

    DeleteResponseAdapter(@Nullable DeleteOneCallback callback) {
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
