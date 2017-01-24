package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.skygear.skygear.Asset;
import io.skygear.skygear.AssetPostRequest;
import io.skygear.skygear.AuthenticationException;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Error;
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.Pubsub;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;
import io.skygear.skygear.Reference;

/**
 * The Container for Chat Plugin
 */
public final class ChatContainer {
    private static final int GET_MESSAGES_DEFAULT_LIMIT = 50; // default value
    private static final String TAG = "SkygearChatContainer";

    private static ChatContainer sharedInstance;

    private final Container skygear;
    private final Map<String, Subscription> messageSubscription = new HashMap<>();
    private final Map<String, Subscription> typingSubscription = new HashMap<>();

    /* --- Constructor --- */

    /**
     * Gets the shared instance.
     *
     * @param container the container
     * @return the instance
     */
    public static ChatContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ChatContainer(container);
        }

        return sharedInstance;
    }

    private ChatContainer(final Container container) {
        if (container != null) {
            this.skygear = container;
        } else {
            throw new NullPointerException("Container can't be null");
        }
    }

    /* --- Conversation --- */

    /**
     * Create a conversation.
     *
     * @param participantIds the participant ids
     * @param title          the title
     * @param metadata       the metadata
     * @param options        the options
     * @param callback       the callback
     */
    public void createConversation(@NonNull final Set<String> participantIds,
                                   @Nullable final String title,
                                   @Nullable final Map<String, Object> metadata,
                                   @Nullable final Map<Conversation.OptionKey, Object> options,
                                   @Nullable final SaveCallback<Conversation> callback) {
        Record record = Conversation.newRecord(participantIds, title, metadata, options);
        skygear.getPublicDatabase().save(record, new SaveResponseAdapter<Conversation>(callback) {
            @Nullable
            @Override
            public Conversation convert(Record record) {
                return new Conversation(record);
            }
        });
    }

    /**
     * Create a direct conversation.
     *
     * @param participantId the participant id
     * @param title         the title
     * @param metadata      the metadata
     * @param callback      the callback
     */
    public void createDirectConversation(@NonNull final String participantId,
                                         @Nullable final String title,
                                         @Nullable final Map<String, Object> metadata,
                                         @Nullable final SaveCallback<Conversation> callback) {
        Set<String> participantIds = new HashSet<>();
        participantIds.add(this.skygear.getCurrentUser().getId());
        participantIds.add(participantId);

        Map<Conversation.OptionKey, Object> options = new HashMap<>();
        options.put(Conversation.OptionKey.DISTINCT_BY_PARTICIPANTS, true);

        Record record = Conversation.newRecord(participantIds, title, metadata, options);
        this.skygear.getPublicDatabase().save(record, new SaveResponseAdapter<Conversation>(callback) {
            @Nullable
            @Override
            public Conversation convert(Record record) {
                return new Conversation(record);
            }
        });
    }

    /**
     * Gets all conversations.
     *
     * @param callback the callback
     */
    public void getConversations(@Nullable final GetCallback<List<Conversation>> callback) {
        this.getUserConversation(new GetCallback<List<UserConversation>>() {
            @Override
            public void onSucc(@Nullable List<UserConversation> userConversations) {
                if (callback == null) {
                    // nothing to do
                    return;
                }

                List<Conversation> conversations = null;
                if (userConversations != null) {
                    conversations = new LinkedList<>();
                    for (UserConversation eachUserConversations : userConversations) {
                        conversations.add(eachUserConversations.getConversation());
                    }
                }
                callback.onSucc(conversations);
            }

            @Override
            public void onFail(@Nullable String failReason) {
                if (callback != null) {
                    callback.onFail(failReason);
                }
            }
        });
    }

    /**
     * Gets conversation.
     *
     * @param conversationId the conversation id
     * @param callback       the callback
     */
    public void getConversation(@NonNull final String conversationId,
                                @Nullable final GetCallback<Conversation> callback) {
        this.getUserConversation(
                conversationId,
                this.skygear.getCurrentUser().getId(),
                true,
                new GetCallback<UserConversation>() {
                    @Override
                    public void onSucc(@Nullable UserConversation userConversation) {
                        if (callback != null) {
                            Conversation conversation = null;
                            if (userConversation != null) {
                                conversation = userConversation.getConversation();
                            }

                            callback.onSucc(conversation);
                        }
                    }

                    @Override
                    public void onFail(@Nullable String failReason) {
                        if (callback != null) {
                            callback.onFail(failReason);
                        }
                    }
                });
    }

    /**
     * Sets conversation title.
     *
     * @param conversation the conversation
     * @param title        the title
     * @param callback     the callback
     */
    public void setConversationTitle(@NonNull final Conversation conversation,
                                     @NonNull final String title,
                                     @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.TITLE_KEY, title);

        this.updateConversation(conversation, map, callback);
    }

    /**
     * Sets conversation admin ids.
     *
     * @param conversation the conversation
     * @param adminIds     the admin ids
     * @param callback     the callback
     */
    public void setConversationAdminIds(@NonNull final Conversation conversation,
                                        @NonNull final Set<String> adminIds,
                                        @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[adminIds.size()];
        adminIds.toArray(ids);
        map.put(Conversation.ADMIN_IDS_KEY, ids);

        this.updateConversation(conversation, map, callback);
    }

    /**
     * Add conversation admin.
     *
     * @param conversation the conversation
     * @param adminId      the admin id
     * @param callback     the callback
     */
    public void addConversationAdmin(@NonNull final Conversation conversation,
                                     @NonNull final String adminId,
                                     @Nullable final SaveCallback<Conversation> callback) {
        Set<String> adminIds = conversation.getAdminIds();
        if (adminIds == null) {
            adminIds = new HashSet<>();
        }
        adminIds.add(adminId);

        this.setConversationAdminIds(conversation, adminIds, callback);
    }

    /**
     * Remove conversation admin.
     *
     * @param conversation the conversation
     * @param adminId      the admin id
     * @param callback     the callback
     */
    public void removeConversationAdmin(@NonNull final Conversation conversation,
                                        @NonNull final String adminId,
                                        @Nullable final SaveCallback<Conversation> callback) {
        Set<String> adminIds = conversation.getAdminIds();
        adminIds.remove(adminId);

        this.setConversationAdminIds(conversation, adminIds, callback);
    }

    /**
     * Sets conversation participants.
     *
     * @param conversation   the conversation
     * @param participantIds the participant ids
     * @param callback       the callback
     */
    public void setConversationParticipants(@NonNull final Conversation conversation,
                                            @NonNull final Set<String> participantIds,
                                            @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[participantIds.size()];
        participantIds.toArray(ids);
        map.put(Conversation.PARTICIPANT_IDS_KEY, ids);

        this.updateConversation(conversation, map, callback);
    }

    /**
     * Add conversation participant.
     *
     * @param conversation  the conversation
     * @param participantId the participant id
     * @param callback      the callback
     */
    public void addConversationParticipant(@NonNull final Conversation conversation,
                                           @NonNull final String participantId,
                                           @Nullable final SaveCallback<Conversation> callback) {
        Set<String> participantIds = conversation.getParticipantIds();
        if (participantIds == null) {
            participantIds = new HashSet<>();
        }
        participantIds.add(participantId);

        this.setConversationParticipants(conversation, participantIds, callback);
    }

    /**
     * Remove conversation participant.
     *
     * @param conversation  the conversation
     * @param participantId the participant id
     * @param callback      the callback
     */
    public void removeConversationParticipant(@NonNull final Conversation conversation,
                                              @NonNull final String participantId,
                                              @Nullable final SaveCallback<Conversation> callback) {
        Set<String> participantIds = conversation.getParticipantIds();
        participantIds.remove(participantId);

        this.setConversationParticipants(conversation, participantIds, callback);
    }

    /**
     * Sets whether the conversation is distinct by participants.
     *
     * @param conversation             the conversation
     * @param isDistinctByParticipants the boolean indicating whether it is distinct by participants
     * @param callback                 the callback
     */
    public void setConversationDistinctByParticipants(@NonNull final Conversation conversation,
                                                      @NonNull final boolean isDistinctByParticipants,
                                                      @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.DISTINCT_BY_PARTICIPANTS_KEY, isDistinctByParticipants);

        this.updateConversation(conversation, map, callback);
    }

    /**
     * Sets conversation metadata.
     *
     * @param conversation the conversation
     * @param metadata     the metadata
     * @param callback     the callback
     */
    public void setConversationMetadata(@NonNull final Conversation conversation,
                                        @NonNull final Map<String, Object> metadata,
                                        @Nullable final SaveCallback<Conversation> callback) {
        JSONObject metadataJSON = new JSONObject(metadata);
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.METADATA_KEY, metadataJSON);

        this.updateConversation(conversation, map, callback);
    }

    /**
     * Leave a conversation.
     *
     * @param conversation the conversation
     * @param callback     the callback
     */
    public void leaveConversation(@NonNull final Conversation conversation,
                                  @Nullable final LambdaResponseHandler callback) {
        this.skygear.callLambdaFunction("chat:leave_conversation",
                                        new Object[]{conversation.getId()},
                                        callback);
    }

    /**
     * Update a conversation.
     *
     * @param conversation the conversation
     * @param updates      the updates
     * @param callback     the callback
     */
    public void updateConversation(@NonNull final Conversation conversation,
                                   @NonNull final Map<String, Object> updates,
                                   @Nullable final SaveCallback<Conversation> callback) {
        final Database publicDB = this.skygear.getPublicDatabase();

        this.getUserConversation(conversation, new GetCallback<UserConversation>() {
            @Override
            public void onSucc(@Nullable UserConversation userConversation) {
                if (callback == null) {
                    // nothing to do
                    return;
                }

                if (userConversation == null) {
                    callback.onFail("Cannot find the conversation");
                    return;
                }

                Record conversationRecord = userConversation.getConversation().record;
                for (Map.Entry<String, Object> entry : updates.entrySet()) {
                    conversationRecord.set(entry.getKey(), entry.getValue());
                }
                publicDB.save(conversationRecord, new SaveResponseAdapter<Conversation>(callback) {
                    @Override
                    public Conversation convert(Record record) {
                        return new Conversation(record);
                    }
                });
            }

            @Override
            public void onFail(@Nullable String failReason) {
                if (callback != null) {
                    callback.onFail(failReason);
                }
            }
        });
    }

    /**
     * Mark last read message of a conversation.
     *
     * @param conversation the conversation
     * @param message      the message
     */
    public void markConversationLastReadMessage(@NonNull final Conversation conversation,
                                                @NonNull final Message message) {
        final Database publicDB = this.skygear.getPublicDatabase();
        this.getUserConversation(conversation, new GetCallback<UserConversation>() {
            @Override
            public void onSucc(@Nullable UserConversation userConversation) {
                if (userConversation == null) {
                    Log.w(TAG, "Cannot find the conversation");
                    return;
                }

                Record userConversationRecord = userConversation.record;
                userConversationRecord.set(
                        UserConversation.LAST_READ_MESSAGE_KEY,
                        Message.newReference(message)
                );

                publicDB.save(userConversationRecord, null);
            }

            @Override
            public void onFail(@Nullable String failReason) {
                Log.i(TAG, "Fail to mark conversation last read message: " + failReason);
            }
        });
    }

    /**
     * Gets unread message count for a conversation.
     *
     * @param conversation the conversation
     * @param callback     the callback
     */
    public void getConversationUnreadMessageCount(@NonNull Conversation conversation,
                                                  @Nullable final GetCallback<Integer> callback) {
        this.getUserConversation(conversation, new GetCallback<UserConversation>() {
            @Override
            public void onSucc(@Nullable UserConversation userConversation) {
                if (callback != null) {
                    if (userConversation == null) {
                        callback.onFail("Cannot find the conversation");
                        return;
                    }

                    callback.onSucc(userConversation.getUnreadCount());
                }
            }

            @Override
            public void onFail(@Nullable String failReason) {
                if (callback != null) {
                    callback.onFail(failReason);
                }
            }
        });
    }

    /**
     * Gets total unread message count.
     *
     * @param callback the callback
     */
    public void getTotalUnreadMessageCount(@Nullable final GetCallback<Integer> callback) {
        this.skygear.callLambdaFunction("chat:total_unread", null, new LambdaResponseHandler() {
            @Override
            public void onLambdaSuccess(JSONObject result) {
                try {
                    int count = result.getInt("message");
                    if (callback != null) {
                        callback.onSucc(count);
                    }
                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onFail(e.getMessage());
                    }
                }
            }

            @Override
            public void onLambdaFail(Error reason) {
                if (callback != null) {
                    callback.onFail(reason.getMessage());
                }
            }
        });
    }

    /* --- User Conversation --- */

    /**
     * Gets the user conversation relation for current user.
     *
     * @param callback the callback
     */
    public void getUserConversation(@Nullable final GetCallback<List<UserConversation>> callback) {
        this.getUserConversation(this.skygear.getCurrentUser().getId(), true, callback);
    }

    /**
     * Gets user conversation relation by a conversation for current user.
     *
     * @param conversation the conversation
     * @param callback     the callback
     */
    public void getUserConversation(@NonNull final Conversation conversation,
                                    @Nullable final GetCallback<UserConversation> callback) {
        this.getUserConversation(
            conversation.getId(),
            this.skygear.getCurrentUser().getId(),
            true,
            callback);
    }

    /**
     * Gets user conversation relation by a conversation and a user.
     *
     * @param conversation the conversation
     * @param user         the user
     * @param callback     the callback
     */
    public void getUserConversation(@NonNull final Conversation conversation,
                                    @NonNull final ChatUser user,
                                    @Nullable final GetCallback<UserConversation> callback) {
        this.getUserConversation(conversation.getId(), user.getId(), true, callback);
    }

    private void getUserConversation(@NonNull final String conversationId,
                                     @NonNull final String userId,
                                     @NonNull final boolean getLastMessages,
                                     @Nullable final GetCallback<UserConversation> callback) {
        Query query = new Query(UserConversation.TYPE_KEY)
                .equalTo(UserConversation.USER_KEY, userId)
                .equalTo(UserConversation.CONVERSATION_KEY, conversationId)
                .transientInclude(UserConversation.USER_KEY)
                .transientInclude(UserConversation.CONVERSATION_KEY);

        this.skygear.getPublicDatabase().query(query, new RecordQueryResponseHandler() {
            @Override
            public void onQuerySuccess(Record[] records) {
                if (callback == null) {
                    // nothing to do
                    return;
                }

                if (records != null && records.length > 0) {
                    final UserConversation uc = new UserConversation(records[0]);
                    String mid = uc.getLastReadMessageId();
                    if (getLastMessages && mid != null) {
                        List<String> messageIds = new ArrayList<String>();
                        messageIds.add(mid);
                        getMessagesByIds(messageIds, new GetCallback<List<Message>>() {
                            @Override
                            public void onSucc(@Nullable List<Message> messages) {
                                if (messages != null && messages.size() > 0) {
                                    uc.lastMessage = messages.get(0);
                                }
                                callback.onSucc(uc);
                            }

                            @Override
                            public void onFail(@Nullable String failReason) {
                                callback.onFail(failReason);
                            }
                        });
                    } else {
                        callback.onSucc(uc);
                    }
                } else {
                    callback.onFail("User Conversation not found");
                }
            }

            @Override
            public void onQueryError(Error reason) {
                if (callback != null) {
                    callback.onFail(reason.getMessage());
                }
            }
        });
    }

    /**
     * Gets user conversation relation for a user.
     *
     * @param user     the user
     * @param callback the callback
     */
    public void getUserConversation(@NonNull final ChatUser user,
                                    @Nullable final GetCallback<List<UserConversation>> callback) {
        this.getUserConversation(user.getId(), true, callback);
    }

    /**
     * Gets user conversation relation for a user, with getLastMessages
     *
     * @param user            the user
     * @param getLastMessages transientInclude the `last_read_message` field
     * @param callback        the callback
     */
    public void getUserConversation(@NonNull final ChatUser user,
                                    @NonNull final Boolean getLastMessages,
                                    @Nullable final GetCallback<List<UserConversation>> callback) {
        this.getUserConversation(user.getId(), getLastMessages, callback);
    }

    private void getUserConversation(@NonNull final String userId,
                                     @NonNull final Boolean getLastMessages,
                                     @Nullable final GetCallback<List<UserConversation>> callback
    ) {
        Query query = new Query(UserConversation.TYPE_KEY)
                .equalTo(UserConversation.USER_KEY, userId)
                .transientInclude(UserConversation.USER_KEY)
                .transientInclude(UserConversation.CONVERSATION_KEY);

        this.skygear.getPublicDatabase().query(query, new RecordQueryResponseHandler() {
            @Override
            public void onQuerySuccess(Record[] records) {
                if (callback == null) {
                    // nothing to do
                    return;
                }

                final List<UserConversation> userConversations = new LinkedList<>();;
                if (records != null && records.length > 0) {
                    for (Record eachRecord : records) {
                        userConversations.add(new UserConversation(eachRecord));
                    }
                }
                if (getLastMessages) {
                    List<String> messageIds = new ArrayList<String>();
                    for (UserConversation uc : userConversations) {
                        String mid = uc.getLastReadMessageId();
                        if (mid != null) {
                            messageIds.add(mid);
                        }
                    }
                    getMessagesByIds(messageIds, new GetCallback<List<Message>>() {
                        @Override
                        public void onSucc(@Nullable List<Message> messages) {
                            Map<String, Message> mMap = new HashMap<String, Message>();
                            for (Message m : messages) {
                                mMap.put(m.getId(), m);
                            }
                            for (UserConversation uc: userConversations) {
                               if (uc.getLastReadMessageId() != null) {
                                   uc.lastMessage = mMap.get(uc.getLastReadMessageId());
                               }
                            }
                            callback.onSucc(userConversations);
                        }

                        @Override
                        public void onFail(@Nullable String failReason) {
                            callback.onFail(failReason);
                        }
                    });
                } else {
                    callback.onSucc(userConversations);
                }
            }

            @Override
            public void onQueryError(Error reason) {
                if (callback != null) {
                    callback.onFail(reason.getMessage());
                }
            }
        });
    }

    /* --- Message --- */

    /**
     * Gets messages.
     *
     * @param conversation the conversation
     * @param limit        the limit
     * @param before       the before
     * @param callback     the callback
     */
    public void getMessages(@NonNull final Conversation conversation,
                            final int limit,
                            @Nullable final Date before,
                            @Nullable final GetCallback<List<Message>> callback) {
        int limitCount = limit;
        String beforeTimeISO8601 = DateUtils.toISO8601(before != null ? before : new Date());

        if (limitCount <= 0) {
            limitCount = GET_MESSAGES_DEFAULT_LIMIT;
        }

        Object[] args = new Object[]{conversation.getId(), limitCount, beforeTimeISO8601};
        this.skygear.callLambdaFunction("chat:get_messages", args, new LambdaResponseHandler() {
            @Override
            public void onLambdaSuccess(JSONObject result) {
                List<Message> messages = null;
                JSONArray results = result.optJSONArray("results");

                if (results != null) {
                    messages = new ArrayList<>(results.length());

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject object = results.getJSONObject(i);
                            Record record = Record.fromJson(object);
                            Message message = new Message(record);
                            messages.add(message);
                        } catch (JSONException e) {
                            Log.e(TAG, "Fail to get message: " + e.getMessage());
                        }
                    }

                    ChatContainer.this.markMessagesAsDelivered(messages);
                }
                if (callback != null) {
                    callback.onSucc(messages);
                }
            }

            @Override
            public void onLambdaFail(Error reason) {
                if (callback != null) {
                    callback.onFail(reason.getMessage());
                }
            }
        });
    }

    /**
     * Gets messages by ids.
     *
     * @param messageIds   List of message ids to be fetch
     * @param callback     the callback
     */
    public void getMessagesByIds(@NonNull final List<String> messageIds,
                                 @Nullable final GetCallback<List<Message>> callback) {

        JSONArray msgJSON = new JSONArray(messageIds);
        Object[] args = new Object[]{msgJSON};
        this.skygear.callLambdaFunction("chat:get_messages_by_ids", args, new LambdaResponseHandler() {
            @Override
            public void onLambdaSuccess(JSONObject result) {
                List<Message> messages = null;
                JSONArray results = result.optJSONArray("results");

                if (results != null) {
                    messages = new ArrayList<>(results.length());

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject object = results.getJSONObject(i);
                            Record record = Record.fromJson(object);
                            Message message = new Message(record);
                            messages.add(message);
                        } catch (JSONException e) {
                            Log.e(TAG, "Fail to get message: " + e.getMessage());
                        }
                    }
                }
                if (callback != null) {
                    callback.onSucc(messages);
                }
            }

            @Override
            public void onLambdaFail(Error reason) {
                if (callback != null) {
                    callback.onFail(reason.getMessage());
                }
            }
        });
    }


    /**
     * Send message.
     *
     * @param conversation the conversation
     * @param body         the body
     * @param asset        the asset
     * @param metadata     the metadata
     * @param callback     the callback
     */
    public void sendMessage(@NonNull final Conversation conversation,
                            @Nullable final String body,
                            @Nullable final Asset asset,
                            @Nullable final JSONObject metadata,
                            @Nullable final SaveCallback<Message> callback) {
        if (!StringUtils.isEmpty(body) || asset != null || metadata != null) {
            Record record = new Record("message");
            Reference reference = new Reference("conversation", conversation.getId());
            record.set("conversation_id", reference);
            if (body != null) {
                record.set("body", body);
            }
            if (metadata != null) {
                record.set("metadata", metadata);
            }

            if (asset == null) {
                this.saveMessageRecord(record, callback);
            } else {
                this.saveMessageRecord(record, asset, callback);
            }
        } else {
            if (callback != null) {
                callback.onFail("Please provide either body, asset or metadata");
            }
        }
    }

    /**
     * Mark a message as read.
     *
     * @param message the message
     */
    public void markMessageAsRead(@NonNull Message message) {
        List<Message> messages = new LinkedList<>();
        messages.add(message);

        this.markMessagesAsRead(messages);
    }

    /**
     * Mark some messages as read.
     *
     * @param messages the messages
     */
    public void markMessagesAsRead(@NonNull List<Message> messages) {
        JSONArray messageIds = new JSONArray();
        for (Message eachMessage : messages) {
            messageIds.put(eachMessage.getId());
        }

        this.skygear.callLambdaFunction(
                "chat:mark_as_read",
                new Object[]{messageIds},
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        Log.i(TAG, "Successfully mark messages as read");
                    }

                    @Override
                    public void onLambdaFail(Error reason) {
                        Log.w(TAG, "Fail to mark messages as read: " + reason.getMessage());
                    }
                });
    }

    /**
     * Mark a message as delivered.
     *
     * @param message the message
     */
    public void markMessageAsDelivered(@NonNull Message message) {
        List<Message> messages = new LinkedList<>();
        messages.add(message);

        this.markMessagesAsDelivered(messages);
    }

    /**
     * Mark some messages as delivered.
     *
     * @param messages the messages
     */
    public void markMessagesAsDelivered(@NonNull List<Message> messages) {
        JSONArray messageIds = new JSONArray();
        for (Message eachMessage : messages) {
            messageIds.put(eachMessage.getId());
        }

        this.skygear.callLambdaFunction(
                "chat:mark_as_delivered",
                new Object[]{messageIds},
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        Log.i(TAG, "Successfully mark messages as delivered");
                    }

                    @Override
                    public void onLambdaFail(Error reason) {
                        Log.w(TAG, "Fail to mark messages as delivered: " + reason.getMessage());
                    }
                });
    }

    private void saveMessageRecord(final Record message,
                                   @Nullable final SaveCallback<Message> callback) {
        try {
            this.skygear.getPrivateDatabase().save(
                    message,
                    new SaveResponseAdapter<Message>(callback) {
                        @Override
                        public Message convert(Record record) {
                            return new Message(record);
                        }
                    }
            );
        } catch (AuthenticationException e) {
            if (callback != null) {
                callback.onFail(e.getMessage());
            }
        }
    }

    private void saveMessageRecord(final Record message,
                                   final Asset asset,
                                   @Nullable final SaveCallback<Message> callback) {
        this.skygear.uploadAsset(asset, new AssetPostRequest.ResponseHandler() {
            @Override
            public void onPostSuccess(Asset asset, String response) {
                message.set("attachment", asset);
                ChatContainer.this.saveMessageRecord(message, callback);
            }

            @Override
            public void onPostFail(Asset asset, Error reason) {
                Log.w(TAG, "Fail to upload asset: " + reason.getMessage());
                ChatContainer.this.saveMessageRecord(message, callback);
            }
        });
    }

    /* --- Message Receipt --- */

    /**
     * Gets the receipts for a message .
     *
     * @param message  the message
     * @param callback the callback
     */
    public void getMessageReceipt(@NonNull final Message message,
                                  @Nullable final GetCallback<List<MessageReceipt>> callback) {
        this.skygear.callLambdaFunction(
                "chat:get_receipt",
                new Object[]{ message.getId() },
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        if (callback == null) {
                            // nothing to do
                            return;
                        }

                        try {
                            List<MessageReceipt> receiptList = new LinkedList<>();
                            JSONArray receipts = result.getJSONArray("receipts");
                            for (int idx = 0; idx < receipts.length(); idx++) {
                                JSONObject eachReceiptJSON = receipts.getJSONObject(idx);
                                receiptList.add(MessageReceipt.fromJSON(eachReceiptJSON));
                            }

                            callback.onSucc(receiptList);
                        } catch (JSONException e) {
                            callback.onFail("Fail to parse the result: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLambdaFail(Error reason) {
                        if (callback != null) {
                            callback.onFail(reason.getMessage());
                        }
                    }
                }
        );
    }

    /* --- Typing --- */

    /**
     * Send typing indicator for a conversation.
     *
     * @param conversation the conversation
     * @param state        the state
     */
    public void sendTypingIndicator(@NonNull Conversation conversation,
                                    @NonNull Typing.State state) {
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
        String timestamp = dateTimeFormatter.print(new DateTime());
        Object[] args = {conversation.getId(), state.getName(), timestamp};
        this.skygear.callLambdaFunction("chat:typing", args, new LambdaResponseHandler(){
            @Override
            public void onLambdaSuccess(JSONObject result) {
                Log.i(TAG, "Successfully send typing indicator");
            }

            @Override
            public void onLambdaFail(Error reason) {
                Log.i(TAG, "Fail to send typing indicator: " + reason.getMessage());
            }
        });
    }

    /**
     * Subscribe typing indicator for a conversation.
     *
     * @param conversation the conversation
     * @param callback     the callback
     */
    public void subscribeTypingIndicator(@NonNull Conversation conversation,
                                         @Nullable final TypingSubscriptionCallback callback) {
        final Pubsub pubsub = this.skygear.getPubsub();
        final String conversationId = conversation.getId();

        if (typingSubscription.get(conversationId) == null) {
            getOrCreateUserChannel(new GetCallback<Record>() {
                @Override
                public void onSucc(@Nullable Record userChannelRecord) {
                    if (userChannelRecord != null) {
                        Subscription subscription = new Subscription(
                                conversationId,
                                (String) userChannelRecord.get("name"),
                                callback
                        );
                        subscription.attach(pubsub);
                        typingSubscription.put(conversationId, subscription);
                    }
                }

                @Override
                public void onFail(@Nullable String failReason) {
                    Log.w(TAG, "Fail to subscribe typing indicator: " + failReason);
                    if (callback != null) {
                        callback.onSubscriptionFail(failReason);
                    }
                }
            });
        }
    }

    /**
     * Unsubscribe typing indicator for a conversation.
     *
     * @param conversation the conversation
     */
    public void unsubscribeTypingIndicator(@NonNull Conversation conversation) {
        final Pubsub pubsub = this.skygear.getPubsub();
        String conversationId = conversation.getId();
        Subscription subscription = typingSubscription.get(conversationId);

        if (subscription != null) {
            subscription.detach(pubsub);
            typingSubscription.remove(conversationId);
        }
    }

    /* --- Chat User --- */

    /**
     * Gets users for the chat plugins.
     *
     * @param callback the callback
     */
    public void getChatUsers(@Nullable final GetCallback<List<ChatUser>> callback) {
        Query query = new Query("user");
        Database publicDB = this.skygear.getPublicDatabase();
        publicDB.query(query, new QueryResponseAdapter<List<ChatUser>>(callback) {
            @Override
            public List<ChatUser> convert(Record[] records) {
                List<ChatUser> users = new ArrayList<>(records.length);

                for (Record record : records) {
                    users.add(new ChatUser(record));
                }

                return users;
            }
        });
    }

    /* --- Subscription--- */

    /**
     * Subscribe conversation message.
     *
     * @param conversation the conversation
     * @param callback     the callback
     */
    public void subscribeConversationMessage(@NonNull final Conversation conversation,
                                             @Nullable final MessageSubscriptionCallback callback) {
        final Pubsub pubsub = this.skygear.getPubsub();
        final String conversationId = conversation.getId();

        if (messageSubscription.get(conversationId) == null) {
            getOrCreateUserChannel(new GetCallback<Record>() {
                @Override
                public void onSucc(@Nullable Record userChannelRecord) {
                    if (userChannelRecord != null) {
                        Subscription subscription = new Subscription(
                                conversationId,
                                (String) userChannelRecord.get("name"),
                                callback
                        );
                        subscription.attach(pubsub);
                        messageSubscription.put(conversationId, subscription);
                    }
                }

                @Override
                public void onFail(@Nullable String failReason) {
                    Log.w(TAG, "Fail to subscribe conversation message: " + failReason);
                    if (callback != null) {
                        callback.onSubscriptionFail(failReason);
                    }
                }
            });
        }
    }

    /**
     * Unsubscribe conversation message.
     *
     * @param conversation the conversation
     */
    public void unsubscribeConversationMessage(@NonNull final Conversation conversation) {
        final Pubsub pubsub = this.skygear.getPubsub();
        String conversationId = conversation.getId();
        Subscription subscription = messageSubscription.get(conversationId);

        if (subscription != null) {
            subscription.detach(pubsub);
            messageSubscription.remove(conversationId);
        }
    }

    private void getOrCreateUserChannel(@Nullable final GetCallback<Record> callback) {
        try {
            Query query = new Query("user_channel");
            Database privateDatabase = this.skygear.getPrivateDatabase();
            privateDatabase.query(query, new RecordQueryResponseHandler() {
                @Override
                public void onQuerySuccess(Record[] records) {
                    if (records.length != 0) {
                        if (callback != null) {
                            callback.onSucc(records[0]);
                        }
                    } else {
                        createUserChannel(callback);
                    }
                }

                @Override
                public void onQueryError(Error reason) {
                    if (callback != null) {
                        callback.onFail(reason.getMessage());
                    }
                }
            });
        } catch (AuthenticationException e) {
            if (callback != null) {
                callback.onFail(e.getMessage());
            }
        }
    }

    private void createUserChannel(final GetCallback<Record> callback) {
        try {
            Record conversation = new Record("user_channel");
            conversation.set("name", UUID.randomUUID().toString());

            RecordSaveResponseHandler handler = new RecordSaveResponseHandler() {
                @Override
                public void onSaveSuccess(Record[] records) {
                    Record record = records[0];
                    if (callback != null) {
                        callback.onSucc(record);
                    }
                }

                @Override
                public void onPartiallySaveSuccess(
                        Map<String, Record> successRecords,
                        Map<String, Error> reasons) {

                }

                @Override
                public void onSaveFail(Error reason) {
                    if (callback != null) {
                        callback.onFail(reason.getMessage());
                    }
                }
            };

            Database db = this.skygear.getPrivateDatabase();
            db.save(conversation, handler);
        } catch (AuthenticationException e) {
            callback.onFail(e.getMessage());
        }
    }

}
