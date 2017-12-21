package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.skygear.Error;
import io.skygear.skygear.RecordDeleteResponseHandler;

/**
 * An adapter converting record delete response to delete object callback
 */
final class DeleteResponseAdapter extends RecordDeleteResponseHandler {
    private final DeleteOneCallback callback;

    /**
     * Instantiates a new delete response adapter.
     *
     * @param callback the callback
     */
    DeleteResponseAdapter(@Nullable DeleteOneCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDeleteSuccess(String[] ids) {
        if (callback != null) {
            callback.onSuccess(ids[0]);
        }
    }

    @Override
    public void onDeletePartialSuccess(String[] ids, Map<String, Error> errors) {

    }

    @Override
    public void onDeleteFail(Error error) {
        if (callback != null) {
            callback.onFail(error);
        }
    }
}
