package io.skygear.plugins.chat.error;

import android.support.annotation.NonNull;

import io.skygear.skygear.Error;

public class MessageOperationError extends Error {
    public MessageOperationError(@NonNull Error error) {
        super(Code.BAD_REQUEST.getValue(), error.getName(), error.getDetailMessage(), error.getInfo());
    }
}
