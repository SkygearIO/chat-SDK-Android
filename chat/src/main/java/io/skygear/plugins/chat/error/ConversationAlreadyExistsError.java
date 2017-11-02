package io.skygear.plugins.chat.error;

import android.support.annotation.NonNull;

import org.json.JSONException;

import io.skygear.plugins.chat.ChatContainer;
import io.skygear.skygear.Error;

public class ConversationAlreadyExistsError extends Error {
    private static final String CONVERSATION_ID_KEY = "conversation_id";
    private final String conversationId;

    public ConversationAlreadyExistsError(@NonNull Error error) throws JSONException{
        super(Code.BAD_REQUEST.getValue(), error.getName(), error.getDetailMessage(), error.getInfo());
        this.conversationId = this.getInfo().getString(CONVERSATION_ID_KEY);
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public static boolean hasConversationId(@NonNull Error error) {
        return error.getInfo() != null && error.getInfo().has(CONVERSATION_ID_KEY);
    }
}
