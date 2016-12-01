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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
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
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.Pubsub;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;
import io.skygear.skygear.Reference;

public final class ChatContainer {
    private static final int GET_MESSAGES_DEFAULT_LIMIT = 50; // default value
    private static final String TAG = "SkygearChatContainer";

    private static ChatContainer sharedInstance;

    private final Container skygear;
    private final Map<String, Subscription> messageSubscription = new HashMap<>();
    private final Map<String, Subscription> typingSubscription = new HashMap<>();

    /* --- Constructor --- */

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

    public void getConversation(@NonNull final String conversationId,
                                @Nullable final GetCallback<Conversation> callback) {
        this.getUserConversation(
                conversationId,
                this.skygear.getCurrentUser().getId(),
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

    public void setConversationTitle(@NonNull final Conversation conversation,
                                     @NonNull final String title,
                                     @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.TITLE_KEY, title);

        this.updateConversation(conversation, map, callback);
    }

    public void setConversationAdminIds(@NonNull final Conversation conversation,
                                        @NonNull final Set<String> adminIds,
                                        @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[adminIds.size()];
        adminIds.toArray(ids);
        map.put(Conversation.ADMIN_IDS_KEY, ids);

        this.updateConversation(conversation, map, callback);
    }

    public void addConversationAdminId(@NonNull final Conversation conversation,
                                       @NonNull final String adminId,
                                       @Nullable final SaveCallback<Conversation> callback) {
        Set<String> adminIds = conversation.getAdminIds();
        if (adminIds == null) {
            adminIds = new HashSet<>();
        }
        adminIds.add(adminId);

        this.setConversationAdminIds(conversation, adminIds, callback);
    }

    public void removeConversationAdminId(@NonNull final Conversation conversation,
                                          @NonNull final String adminId,
                                          @Nullable final SaveCallback<Conversation> callback) {
        Set<String> adminIds = conversation.getAdminIds();
        if (adminIds == null) {
            adminIds = new HashSet<>();
        }
        adminIds.remove(adminId);

        this.setConversationAdminIds(conversation, adminIds, callback);
    }

    public void setConversationParticipantsIds(@NonNull final Conversation conversation,
                                               @NonNull final Set<String> participantIds,
                                               @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[participantIds.size()];
        participantIds.toArray(ids);
        map.put(Conversation.PARTICIPANT_IDS_KEY, ids);

        this.updateConversation(conversation, map, callback);
    }

    public void addConversationParticipantId(@NonNull final Conversation conversation,
                                             @NonNull final String participantId,
                                             @Nullable final SaveCallback<Conversation> callback) {
        Set<String> participantIds = conversation.getParticipantIds();
        if (participantIds == null) {
            participantIds = new HashSet<>();
        }
        participantIds.add(participantId);

        this.setConversationParticipantsIds(conversation, participantIds, callback);
    }

    public void removeConversationParticipantId(@NonNull final Conversation conversation,
                                                @NonNull final String participantId,
                                                @Nullable final SaveCallback<Conversation> callback) {
        Set<String> participantIds = conversation.getParticipantIds();
        if (participantIds == null) {
            participantIds = new HashSet<>();
        }
        participantIds.remove(participantId);

        this.setConversationParticipantsIds(conversation, participantIds, callback);
    }

    public void setConversationDistinctByParticipants(@NonNull final Conversation conversation,
                                                      @NonNull final boolean isDistinctByParticipants,
                                                      @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.DISTINCT_BY_PARTICIPANTS_KEY, isDistinctByParticipants);

        this.updateConversation(conversation, map, callback);
    }

    public void setConversationMetadata(@NonNull final Conversation conversation,
                                        @NonNull final Map<String, Object> metadata,
                                        @Nullable final SaveCallback<Conversation> callback) {
        JSONObject metadataJSON = new JSONObject(metadata);
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.METADATA_KEY, metadataJSON);

        this.updateConversation(conversation, map, callback);
    }

    public void leaveConversation(@NonNull final Conversation conversation,
                                  @Nullable final LambdaResponseHandler callback) {
        this.skygear.callLambdaFunction("chat:leave_conversation",
                                        new Object[]{conversation.getId()},
                                        callback);
    }

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
            public void onLambdaFail(String reason) {
                if (callback != null) {
                    callback.onFail(reason);
                }
            }
        });
    }

    /* --- User Conversation --- */

    public void getUserConversation(@Nullable final GetCallback<List<UserConversation>> callback) {
        this.getUserConversation(this.skygear.getCurrentUser().getId(), callback);
    }

    public void getUserConversation(@NonNull final Conversation conversation,
                                    @Nullable final GetCallback<UserConversation> callback) {
        this.getUserConversation(conversation.getId(), this.skygear.getCurrentUser().getId(), callback);
    }

    public void getUserConversation(@NonNull final Conversation conversation,
                                    @NonNull final ChatUser user,
                                    @Nullable final GetCallback<UserConversation> callback) {
        this.getUserConversation(conversation.getId(), user.getId(), callback);
    }

    private void getUserConversation(@NonNull final String conversationId,
                                     @NonNull final String userId,
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

                UserConversation userConversation = null;
                if (records != null && records.length > 0) {
                    userConversation = new UserConversation(records[0]);
                }
                callback.onSucc(userConversation);
            }

            @Override
            public void onQueryError(String reason) {
                if (callback != null) {
                    callback.onFail(reason);
                }
            }
        });
    }

    public void getUserConversation(@NonNull final ChatUser user,
                                    @Nullable final GetCallback<List<UserConversation>> callback) {
        this.getUserConversation(user.getId(), callback);
    }

    private void getUserConversation(@NonNull final String userId,
                                     @Nullable final GetCallback<List<UserConversation>> callback) {
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

                List<UserConversation> userConversations = null;
                if (records != null && records.length > 0) {
                    userConversations = new LinkedList<>();
                    for (Record eachRecord : records) {
                        userConversations.add(new UserConversation(eachRecord));
                    }
                }
                callback.onSucc(userConversations);
            }

            @Override
            public void onQueryError(String reason) {
                if (callback != null) {
                    callback.onFail(reason);
                }
            }
        });
    }

    /* --- Message --- */

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
            public void onLambdaFail(String reason) {
                if (callback != null) {
                    callback.onFail(reason);
                }
            }
        });
    }

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

    public void markMessageAsRead(@NonNull Message message) {
        List<Message> messages = new LinkedList<>();
        messages.add(message);

        this.markMessagesAsRead(messages);
    }

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
                    public void onLambdaFail(String reason) {
                        Log.w(TAG, "Fail to mark messages as read: " + reason);
                    }
                });
    }

    public void markMessageAsDelivered(@NonNull Message message) {
        List<Message> messages = new LinkedList<>();
        messages.add(message);

        this.markMessagesAsDelivered(messages);
    }

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
                    public void onLambdaFail(String reason) {
                        Log.w(TAG, "Fail to mark messages as delivered: " + reason);
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
            public void onPostFail(Asset asset, String reason) {
                Log.w(TAG, "Fail to upload asset: " + reason);
                ChatContainer.this.saveMessageRecord(message, callback);
            }
        });
    }

    /* --- Message Receipt --- */

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
                    public void onLambdaFail(String reason) {
                        if (callback != null) {
                            callback.onFail(reason);
                        }
                    }
                }
        );
    }

    /* --- Typing --- */

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
            public void onLambdaFail(String reason) {
                Log.i(TAG, "Fail to send typing indicator: " + reason);
            }
        });
    }

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
                public void onQueryError(String reason) {
                    if (callback != null) {
                        callback.onFail(reason);
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
                        Map<String, String> reasons) {

                }

                @Override
                public void onSaveFail(String reason) {
                    if (callback != null) {
                        callback.onFail(reason);
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
