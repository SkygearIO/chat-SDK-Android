package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;
import io.skygear.skygear.User;

/**
 * The User Conversation Relation model for Chat Plugin.
 */
class UserConversation {
    // TODO: Implement RecordWrapper when it is available

    static final String TYPE_KEY = "user_conversation";
    static final String CONVERSATION_KEY = "conversation";
    static final String USER_KEY = "user";
    static final String UNREAD_COUNT_KEY = "unread_count";
    static final String LAST_READ_MESSAGE_KEY = "last_read_message";

    final Record record;
    UserConversation(@NonNull final Record record) {
        super();
        this.record = record;
    }

    /**
     * Instantiates a new conversation from a Skygear Record.
     *
     * @param record the record
     */

    @NonNull
    static Conversation GetConversationByUserConversationRecord(@NonNull final Record record)
    {
        Object conversationObject = record.getTransient().get(CONVERSATION_KEY);
        int unreadCount = (int) record.get(UNREAD_COUNT_KEY);
        Reference lastReadMessageKey = (Reference) record.get(LAST_READ_MESSAGE_KEY);
        String lastReadMessageId = lastReadMessageKey == null ? null : lastReadMessageKey.getId();
        return new Conversation((Record) conversationObject, unreadCount, lastReadMessageId);
    }
}
