package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.skygear.skygear.Database;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;

/**
 * The User Conversation Relation model for Chat Plugin.
 */
final class UserConversation {
    static final String TYPE_KEY = "user_conversation";
    static final String CONVERSATION_KEY = "conversation";
    static final String USER_KEY = "user";
    static final String UNREAD_COUNT_KEY = "unread_count";
    static final String LAST_READ_MESSAGE_KEY = "last_read_message";

    final Record record;

    /**
     * Instantiates a new user conversation relation from a Skygear Record.
     *
     * @param record the record
     */
    UserConversation(@NonNull final Record record) {
        super();

        this.record = record;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return this.record.getId();
    }

    /**
     * Gets conversation.
     *
     * @return the conversation
     */
    @Nullable
    public Conversation getConversation() {
        Object conversationObject = this.record.getTransient().get(CONVERSATION_KEY);
        if (conversationObject == null) {
            return null;
        }

        return new Conversation((Record) conversationObject);
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    @Nullable
    public ChatUser getUser() {
        Object chatUserObject = this.record.getTransient().get(USER_KEY);
        if (chatUserObject == null) {
            return null;
        }

        return new ChatUser((Record) chatUserObject);
    }

    /**
     * Gets unread count.
     *
     * @return the unread count
     */
    public int getUnreadCount() {
        return (int) this.record.get(UNREAD_COUNT_KEY);
    }
}
