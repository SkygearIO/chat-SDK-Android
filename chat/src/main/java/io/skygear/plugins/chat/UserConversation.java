package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

/**
 * The User Conversation Relation model for Chat Plugin.
 */
public class UserConversation {
    // TODO: Implement RecordWrapper when it is available

    static final String TYPE_KEY = "user_conversation";
    static final String CONVERSATION_KEY = "conversation";
    static final String USER_KEY = "user";
    static final String UNREAD_COUNT_KEY = "unread_count";
    static final String LAST_READ_MESSAGE_KEY = "last_read_message";

    final Record record;
    Message lastMessage;
    Conversation conversation;

    /**
     * Instantiates a new user conversation relation from a Skygear Record.
     *
     * @param record the record
     */
    UserConversation(@NonNull final Record record) {
        super();

        this.record = record;
        this.lastMessage = null;
        Object conversationObject = this.record.getTransient().get(CONVERSATION_KEY);
        if (conversationObject == null) {
            this.conversation = null;
        } else {
            this.conversation = new Conversation((Record) conversationObject);
        }
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
        return this.conversation;
    }
    public void setConversation(Conversation c) {
        this.conversation = c;
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

    /**
     * Gets Last read message.
     *
     * @return the user
     */
    @Nullable
    public Message getLastReadMessage() {
        return this.lastMessage;
    }

    /**
     * Gets last_read_message id.
     *
     * @return the message id without type
     */
    public String getLastReadMessageId() {
        Reference ref = (Reference) this.record.get(LAST_READ_MESSAGE_KEY);
        if (ref != null) {
            return ref.getId();
        }
        return null;
    }
    /**
     * Gets record.
     *
     * @return the Skygear record
     */
    public Record getRecord() {
        return record;
    }
}
