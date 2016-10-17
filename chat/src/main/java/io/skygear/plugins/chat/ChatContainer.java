package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Container;

public final class ChatContainer {
    private static ChatContainer sharedInstance;

    private final ConversationContainer conversationContainer;
    private final MessageContainer messageContainer;
    private final ChatUserContainer chatUserContainer;
    private final SubContainer subContainer;
    private final UnreadContainer unreadContainer;

    /**
     * Gets the ChatUser container of Chat Plugin shared within the application.
     *
     * @param container - skygear context
     * @return a Conversation container
     */
    public static ChatContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ChatContainer(container);
        }

        return sharedInstance;
    }

    private ChatContainer(final Container container) {
        if (container != null) {
            this.conversationContainer = ConversationContainer.getInstance(container);
            this.messageContainer = MessageContainer.getInstance(container);
            this.chatUserContainer = ChatUserContainer.getInstance(container);
            this.subContainer = SubContainer.getInstance(container);
            this.unreadContainer = UnreadContainer.getInstance(container);
        } else {
            throw new NullPointerException("Container can't be null");
        }
    }


    /**
     * Create a new conversation.
     *
     * @param participantIds - the participants ids
     * @param adminIds - the admin ids
     * @param title - the title
     * @param callback - SaveCallback&lt;Conversation&gt; to handle new conversation
     */
    public void createConversation(@Nullable final List<String> participantIds,
                                   @Nullable final List<String> adminIds,
                                   @Nullable final String title,
                                   @Nullable final SaveCallback<Conversation> callback) {
        conversationContainer.create(participantIds, adminIds, title, callback);
    }

    /**
     * Gets all conversations where the user joined.
     *
     * @param callback - GetCallback&lt;List&lt;Conversation&gt;&gt; to handle result conversations
     */
    public void getAllConversations(@Nullable final GetCallback<List<Conversation>> callback) {
        conversationContainer.getAll(callback);
    }

    /**
     * Gets a conversation by id.
     *
     * @param conversationId - the conversation id
     * @param callback - GetCallback&lt;Conversation&gt; to handle result conversation
     */
    public void getConversation(@NonNull final String conversationId,
                                @Nullable final GetCallback<Conversation> callback) {
        conversationContainer.get(conversationId, callback);
    }

    /**
     * Update a conversation title by conversation id.
     *
     * @param conversationId - the conversation id
     * @param title - the new title
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    public void setConversationTitle(@NonNull final String conversationId,
                                     @NonNull final String title,
                                     @Nullable final SaveCallback<Conversation> callback) {
        conversationContainer.setTitle(conversationId, title, callback);
    }

    /**
     * Update a conversation admin ids by conversation id.
     *
     * @param conversationId - the conversation id
     * @param adminIds - the new admin ids
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    public void setConversationAdminIds(@NonNull final String conversationId,
                                        @NonNull final List<String> adminIds,
                                        @Nullable final SaveCallback<Conversation> callback) {
        conversationContainer.setAdminIds(conversationId, adminIds, callback);
    }

    /**
     * Update a conversation participant ids by conversation id.
     *
     * @param conversationId - the conversation id
     * @param participantIds - the new participant ids
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    public void setConversationParticipantsIds(@NonNull final String conversationId,
                                               @NonNull final List<String> participantIds,
                                               @Nullable final SaveCallback<Conversation> callback) {
        conversationContainer.setParticipantsIds(conversationId, participantIds, callback);
    }

    /**
     * Delete a conversation by id.
     *
     * @param conversationId - the conversation id
     * @param callback - DeleteOneCallback to handle delete result
     */
    public void deleteConversation(@NonNull final String conversationId,
                                   @Nullable final DeleteOneCallback callback) {
        conversationContainer.delete(conversationId, callback);
    }

    /**
     * Mark the last read message of a conversation.
     *
     * @param conversationId - the conversation id
     * @param messageId - the last read message id
     */
    public void markConversationLastReadMessage(@NonNull final String conversationId,
                                                @NonNull final String messageId) {
        conversationContainer.markLastReadMessage(conversationId, messageId);
    }

    /**
     * Gets all messages of a conversation.
     *
     * @param conversationId - the conversation id
     * @param limit - the limit of number of messages, default value is 50
     * @param before - get the messages before the Date instance
     * @param callback - GetCallback&lt;List&lt;Message&gt;&gt; to handle messages
     */
    public void getAllMessages(@NonNull final String conversationId,
                               final int limit,
                               @Nullable final Date before,
                               @Nullable final GetCallback<List<Message>> callback) {
        messageContainer.getAll(conversationId, limit, before, callback);
    }

    /**
     * Send a message to a conversation.
     *
     * @param conversationId - the conversation id
     * @param body - the message body
     * @param asset - the message asset
     * @param metadata - the message metadata
     * @param callback - SaveCallback&lt;Message&gt; to handle send result
     *
     * Either body, asset or metadata can't be null
     */
    public void sendMessage(@NonNull final String conversationId,
                            @Nullable final String body,
                            @Nullable final Asset asset,
                            @Nullable final JSONObject metadata,
                            @Nullable final SaveCallback<Message> callback) {
        messageContainer.send(conversationId, body, asset, metadata, callback);
    }

    /**
     * Gets all chat users on the skygear.
     *
     * @param callback - GetCallback&lt;List&lt;ChatUser&gt;&gt; to handle result chat users
     */
    public void getAllChatUsers(@Nullable final GetCallback<List<ChatUser>> callback) {
        chatUserContainer.getAll(callback);
    }

    /**
     * Subscribe message changing of a conversation.
     *
     * @param conversationId - Conversation Id
     * @param callback - SubCallback instance to handle Message subscription
     */
    public void subConversationMessage(@NonNull final String conversationId,
                                       @Nullable final SubCallback<Message> callback) {
        subContainer.sub(conversationId, callback);
    }

    /**
     * Un-Subscribe message changing of a conversation.
     *
     * @param conversationId - Conversation Id
     */
    public void unSubConversationMessage(@NonNull final String conversationId) {
        subContainer.unSub(conversationId);
    }

    /**
     * Gets the total unread count.
     *
     * @param callback - GetCallback instance to handle Unread instance
     */
    public void getTotalUnread(@Nullable final GetCallback<Unread> callback) {
        unreadContainer.get(callback);
    }
}
