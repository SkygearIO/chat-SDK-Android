package io.skygear.plugins.chat.resps;


import android.support.annotation.Nullable;

import java.util.Map;

import io.skygear.plugins.chat.callbacks.DeleteOneCallback;
import io.skygear.skygear.RecordDeleteResponseHandler;

/**
 * The Record <b>Delete</b> Response Handler.
 */
public final class DeleteResp extends RecordDeleteResponseHandler {
    private final DeleteOneCallback callback;

    /**
     * Default constructor of DeleteResp
     *
     * @param callback - the DeleteOneCallback callback
     */
    public DeleteResp(@Nullable DeleteOneCallback callback) {
        this.callback = callback;
    }

    /**
     * Delete success callback.
     *
     * @param ids the deleted record ids
     */
    @Override
    public void onDeleteSuccess(String[] ids) {
        if (callback != null) {
            callback.onSucc(ids[0]);
        }
    }

    /**
     * Partially delete success callback.
     *
     * @param ids     the deleted record ids
     * @param reasons the fail reason map (recordId to reason String)
     */
    @Override
    public void onDeletePartialSuccess(String[] ids, Map<String, String> reasons) {

    }

    /**
     * Delete fail callback.
     *
     * @param reason the reason
     */
    @Override
    public void onDeleteFail(String reason) {
        if (callback != null) {
            callback.onFail(reason);
        }
    }
}
