package io.skygear.plugins.chat.error;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.skygear.skygear.Error;

public class ConversationOperationError extends Error {
    public ConversationOperationError(@NonNull Error error) {
        super(Code.BAD_REQUEST.getValue(), error.getName(), error.getDetailMessage(), error.getInfo());
    }
}
