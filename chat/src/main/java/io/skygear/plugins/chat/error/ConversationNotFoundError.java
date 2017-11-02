package io.skygear.plugins.chat.error;

import android.support.annotation.NonNull;

import io.skygear.skygear.Error;

public class ConversationNotFoundError extends Error {
    public ConversationNotFoundError(@NonNull String conversationID) {
        super(Code.RESOURCE_NOT_FOUND.getValue(), String.format("Conversation %s not found", conversationID));
    }
}
