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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.realm.Realm;
import io.skygear.plugins.chat.error.TotalUnreadError;
import io.skygear.skygear.Asset;
import io.skygear.skygear.AssetPostRequest;
import io.skygear.skygear.AuthenticationException;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Error;
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.PubsubContainer;
import io.skygear.skygear.PubsubHandler;
import io.skygear.skygear.PubsubListener;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;
import io.skygear.skygear.Reference;
import io.skygear.plugins.chat.error.JSONError;
import io.skygear.plugins.chat.error.ConversationNotFoundError;
import io.skygear.plugins.chat.error.ConversationOperationError;
import io.skygear.plugins.chat.error.ConversationAlreadyExistsError;
import io.skygear.plugins.chat.error.MessageOperationError;
import io.skygear.plugins.chat.error.InvalidMessageError;
import io.skygear.plugins.chat.error.AuthenticationError;

/**
 * The Container for Chat Plugin
 */
public final class ChatContainer {
    private static final int GET_MESSAGES_DEFAULT_LIMIT = 50; // default value
    private static final String TAG = "SkygearChatContainer";


    private static ChatContainer sharedInstance;

    private final Container skygear;
    private final CacheController cacheController;
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
            CacheController cacheController = CacheController.getInstance();
            sharedInstance = new ChatContainer(container, cacheController);
        }

        return sharedInstance;
    }

    private ChatContainer(final Container container, CacheController cacheController) {
        if (container != null) {
            this.skygear = container;
        } else {
            throw new NullPointerException("Container can't be null");
        }

        Realm.init(container.getContext());
        // Since we are initiating Realm, we make use of this opportunity to clean
        // up the cache.
        cacheController.cleanUpOnLaunch();
        this.cacheController = cacheController;
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
        this.skygear.callLambdaFunction("chat:create_conversation",
                new Object[] {
                        new JSONArray(participantIds),
                        title,
                        metadata == null ? null : new JSONObject(metadata),
                        options == null ? null : new JSONObject(convertOptionsMap(options))
                },
                new LambdaResponseHandler(){
                    @Override
                    public void onLambdaSuccess(JSONObject result){
                        try {
                            Conversation conversation = Conversation.fromJson((JSONObject) result.get("conversation"));
                            if (callback != null) {
                                callback.onSuccess(conversation);
                            }
                        } catch (JSONException e)
                        {
                            if (callback != null) {
                                callback.onFail(new JSONError());
                            }
                        }
                    }

                    @Override
                    public void onLambdaFail(Error error) {
                        if (callback != null) {
                            if (ConversationAlreadyExistsError.hasConversationId(error)) {
                                try {
                                    callback.onFail(new ConversationAlreadyExistsError(error));
                                } catch (JSONException e) {
                                    callback.onFail(new JSONError());
                                }
                            } else {
                                callback.onFail(new ConversationOperationError(error));
                            }
                        }
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
        participantIds.add(this.skygear.getAuth().getCurrentUser().getId());
        participantIds.add(participantId);

        Map<Conversation.OptionKey, Object> options = new HashMap<>();
        options.put(Conversation.OptionKey.DISTINCT_BY_PARTICIPANTS, true);
        createConversation(participantIds, title, metadata, options, callback);
    }

    private Map<String, Object> convertOptionsMap(Map<Conversation.OptionKey, Object> options) {
        HashMap<String, Object> newOptions = new HashMap<String, Object>();
        for (Map.Entry<Conversation.OptionKey, Object> option: options.entrySet()) {
            newOptions.put(option.getKey().getValue(), option.getValue());
        }
        return newOptions;
    }


    /**
     * Gets all conversations with last_message and last_read_message.
     *
     * @param callback the callback
     */

    public void getConversations(@Nullable final GetCallback<List<Conversation>> callback) {
        this.getConversations(callback, true);
    }


    /**
     * Gets conversation.
     *
     * @param conversationId the conversation id
     * @param callback       the callback
     * @param getLastMessage get last_message and last_read_message if getLastMessage is true
     */
    public void getConversation(@NonNull final String conversationId,
                                @Nullable final GetCallback<Conversation> callback, boolean getLastMessage) {
        this.getConversation(
                conversationId,
                getLastMessage,
                new GetCallback<Conversation>() {
                    @Override
                    public void onSuccess(@Nullable Conversation conversation) {
                        if (callback != null) {

                            callback.onSuccess(conversation);
                        }
                    }

                    @Override
                    public void onFail(@NonNull Error error) {
                        if (callback != null) {
                            callback.onFail(error);
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
        this.getConversation(conversationId, true, callback);
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


    private void updateConversationMembership(@NonNull final Conversation conversation,
                                              @NonNull final String lambda,
                                              @NonNull final List<String> memberIds,
                                              @Nullable final SaveCallback<Conversation> callback)
    {
        this.skygear.callLambdaFunction(lambda,
                new Object[]{conversation.getId(), new JSONArray(memberIds)},
                new LambdaResponseHandler(){
                    @Override
                    public void onLambdaSuccess(JSONObject result){
                        try {
                            Conversation conversation = Conversation.fromJson((JSONObject) result.get("conversation"));
                            if (callback != null) {
                                callback.onSuccess(conversation);
                            }
                        } catch (JSONException e)
                        {
                            if (callback != null) {
                                callback.onFail(new JSONError());
                            }
                        }
                    }

                    @Override
                    public void onLambdaFail(Error error) {

                        if (callback != null) {
                            callback.onFail(new ConversationOperationError(error));
                        }
                    }
                });

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
        addConversationAdmins(conversation, Arrays.asList(adminId), callback);
    }

    /**
     * Add conversation admins.
     *
     * @param conversation the conversation
     * @param adminIds     the admin ids
     * @param callback     the callback
     */
    public void addConversationAdmins(@NonNull final Conversation conversation,
                                      @NonNull final List<String> adminIds,
                                      @Nullable final SaveCallback<Conversation> callback) {
        updateConversationMembership(conversation, "chat:add_admins", adminIds, callback);
    }

    /**
     * Remove conversation admins.
     *
     * @param conversation the conversation
     * @param adminIds      the admin ids
     * @param callback     the callback
     */
    public void removeConversationAdmins(@NonNull final Conversation conversation,
                                        @NonNull final List<String> adminIds,
                                        @Nullable final SaveCallback<Conversation> callback) {
        updateConversationMembership(conversation, "chat:remove_admins", adminIds, callback);
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
        removeConversationAdmins(conversation, Arrays.asList(adminId), callback);
    }


    /**
     * Add conversation participants.
     *
     * @param conversation  the conversation
     * @param participantIds the participant ids
     * @param callback      the callback
     */
    public void addConversationParticipants(@NonNull final Conversation conversation,
                                           @NonNull final List<String> participantIds,
                                           @Nullable final SaveCallback<Conversation> callback) {
        updateConversationMembership(conversation, "chat:add_participants", participantIds, callback);
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
        addConversationParticipants(conversation, Arrays.asList(participantId), callback);
    }

    /**
     * Remove conversation participants.
     *
     * @param conversation  the conversation
     * @param participantIds the participant ids
     * @param callback      the callback
     */
    public void removeConversationParticipants(@NonNull final Conversation conversation,
                                               @NonNull final List<String> participantIds,
                                               @Nullable final SaveCallback<Conversation> callback) {
        updateConversationMembership(conversation, "chat:remove_participants", participantIds, callback);
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
        removeConversationParticipants(conversation, Arrays.asList(participantId), callback);
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
     * Delete a conversation.
     *
     * @param conversation  the conversation
     * @param callback     the callback
     */
    public void deleteConversation(@NonNull final Conversation conversation,
                                  @Nullable final DeleteCallback<Boolean> callback) {
        this.skygear.callLambdaFunction("chat:delete_conversation",
                new Object[]{conversation.getId()},
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onLambdaFail(Error error) {
                        if (callback != null) {
                            callback.onFail(new ConversationOperationError(error));
                        }
                    }
                });
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
        final String conversationId = conversation.getId();
        this.getConversation(conversationId, true, new GetCallback<Conversation>() {
            @Override
            public void onSuccess(@Nullable final Conversation conversation) {
                if (callback == null) {
                    // nothing to do
                    return;
                }

                if (conversation == null) {
                    callback.onFail(new ConversationNotFoundError(conversationId));
                    return;
                }

                Record conversationRecord = conversation.record;
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
            public void onFail(Error error) {
                if (callback != null) {
                    callback.onFail(new ConversationOperationError(error));
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
        markMessagesAsRead(Arrays.asList(new Message[]{message}));
    }

    /**
     * Gets total unread message count.
     *
     * @param callback the callback
     */
    public void getTotalUnreadMessageCount(@Nullable final GetCallback<Integer> callback) {
        this.skygear.callLambdaFunction("chat:total_unread", new LambdaResponseHandler() {
            @Override
            public void onLambdaSuccess(JSONObject result) {
                try {
                    int count = result.getInt("message");
                    if (callback != null) {
                        callback.onSuccess(count);
                    }
                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onFail(new JSONError());
                    }
                }
            }

            @Override
            public void onLambdaFail(Error error) {
                if (callback != null) {
                    callback.onFail(new TotalUnreadError(error));
                }
            }
        });
    }

    /* --- Conversation (Private) --- */

    /**
     * Gets single conversation for current user.
     *
     * @param conversationId  the ID of conversation
     * @param getLastMessages if true, then last_message and last_read_message are fetched.
     * @param callback        the callback
     */

    private void getConversation(@NonNull final String conversationId,
                                      @NonNull final boolean getLastMessages,
                                      @Nullable final GetCallback<Conversation> callback) {
        this.skygear.callLambdaFunction("chat:get_conversation",
                new Object[]{conversationId, getLastMessages},
                new LambdaResponseHandler(){
                    @Override
                    public void onLambdaSuccess(JSONObject result){
                        try {
                            Conversation conversation = Conversation.fromJson(result.getJSONObject("conversation"));
                            if (callback != null) {
                                callback.onSuccess(conversation);
                            }
                        } catch (JSONException e)
                        {
                            if (callback != null) {
                                callback.onFail(new JSONError());
                            }
                        }
                    }

                    @Override
                    public void onLambdaFail(Error error) {

                        if (callback != null) {
                            callback.onFail(new ConversationNotFoundError(conversationId));
                        }
                    }
                });
    }

    /**
     * Gets all conversations for current user.
     *
     * @param getLastMessages if true, then last_message and last_read_message are fetched.
     * @param callback        the callback
     */

    public void getConversations(@Nullable final GetCallback<List<Conversation>> callback,
                                  @NonNull final Boolean getLastMessages
    ) {
        this.skygear.callLambdaFunction("chat:get_conversations",
                new Object[]{1, 50, getLastMessages},
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        try {
                            JSONArray items = result.getJSONArray("conversations");
                            ArrayList<Conversation> conversations = new ArrayList<>();
                            int n = items.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject o = items.getJSONObject(i);
                                conversations.add(Conversation.fromJson(o));
                            }
                            if (callback != null) {
                                callback.onSuccess(conversations);
                            }
                        } catch (JSONException e) {
                            if (callback != null) {
                                callback.onFail(new JSONError());
                            }
                        }
                    }

                    @Override
                    public void onLambdaFail(Error error) {

                        if (callback != null) {
                            callback.onFail(new ConversationOperationError(error));
                        }
                    }
                });
    }

    /**
     * Gets messages.
     *
     * @param conversation  the conversation
     * @param limit         the limit
     * @param beforeMessage the before message
     * @param order         the order, either 'edited_at' or '_created_at'
     * @param callback      the callback
     */
    public void getMessages(@NonNull final Conversation conversation,
                            final int limit,
                            @Nullable final Message beforeMessage,
                            @Nullable final String order,
                            @Nullable final GetMessagesCallback callback) {
        getMessages(conversation, limit, beforeMessage == null ? null : beforeMessage.getId(), order, callback);
    }

    /**
     * Gets messages.
     *
     * @param conversation     the conversation
     * @param limit            the limit
     * @param beforeMessageId  the before message
     * @param order            the order, either 'edited_at' or '_created_at'
     * @param callback         the callback
     */
    public void getMessages(@NonNull final Conversation conversation,
                            final int limit,
                            @Nullable final String beforeMessageId,
                            @Nullable final String order,
                            @Nullable final GetMessagesCallback callback) {
        int limitCount = limit;
        if (limitCount <= 0) {
            limitCount = GET_MESSAGES_DEFAULT_LIMIT;
        }

        cacheController.getMessages(conversation, limitCount, beforeMessageId, order, CreateGetCallback(callback));

        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("conversation_id", conversation.getId());
        args.put("limit", limitCount);
        args.put("before_message_id", beforeMessageId);
        args.put("order", order);
        getMessages(args, callback);

    }

    /* --- Message --- */
    /**
     * Gets messages.
     * 
     * @param conversation the conversation
     * @param limit        the limit
     * @param before       the before
     * @param order        the order, either 'edited_at' or '_created_at'
     * @param callback     the callback
     */
    public void getMessages(@NonNull final Conversation conversation,
                            final int limit,
                            @Nullable final Date before,
                            @Nullable final String order,
                            @Nullable final GetMessagesCallback callback) {
        int limitCount = limit;
        String beforeTimeISO8601 = DateUtils.toISO8601(before != null ? before : new Date());

        if (limitCount <= 0) {
            limitCount = GET_MESSAGES_DEFAULT_LIMIT;
        }

        cacheController.getMessages(conversation, limitCount, before, order, CreateGetCallback(callback));

        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("conversation_id", conversation.getId());
        args.put("limit", limitCount);
        args.put("before_time", beforeTimeISO8601);
        args.put("order", order);
        getMessages(args, callback);

    }

    private GetCallback<List<Message>> CreateGetCallback(@Nullable  final GetMessagesCallback callback) {
        return new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> object) {
                if (callback != null) {
                    callback.onGetCachedResult(object);
                }
            }

            @Override
            public void onFail(@NonNull Error error) {
                Log.e(TAG, "Failed to load message from cache: " + error.getMessage());
            }
        };
    }

    private void getMessages(@NonNull HashMap<String, Object> args, @Nullable final GetMessagesCallback callback) {
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
                Message[] messageArray = new Message[messages.size()];
                cacheController.didGetMessages(messages.toArray(messageArray), new Message[]{});
                if (callback != null) {
                    callback.onSuccess(messages);
                }
            }

            @Override
            public void onLambdaFail(Error error) {
                if (callback != null) {
                    callback.onFail(new MessageOperationError(error));
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
            Message message = new Message(record);
            if (body != null) {
                message.setBody(body);
            }
            if (asset != null) {
                message.setAsset(asset);
            }
            if (metadata != null) {
                message.setMetadata(metadata);
            }

            this.addMessage(message, conversation, callback);
        } else {
            if (callback != null) {
                callback.onFail(new InvalidMessageError());
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
                    public void onLambdaFail(Error error) {
                        Log.w(TAG, "Fail to mark messages as read: " + error.getMessage());
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
                    public void onLambdaFail(Error error) {
                        Log.w(TAG, "Fail to mark messages as delivered: " + error.getMessage());
                    }
                });
    }

    /**
     * Add Message to conversation
     *
     * @param message the message to be edited
     * @param conversation the conversation
     * @param callback save callback
     */

    public void addMessage(@NonNull Message message,
                           @NonNull final Conversation conversation,
                           @Nullable final SaveCallback<Message> callback)
    {
        Reference reference = new Reference("conversation", conversation.getId());
        message.record.set("conversation", reference);
        message.sendDate = new Date();

        if (message.getAsset() == null) {
            this.saveMessage(message, true, callback);
        } else {
            this.saveMessage(message, message.getAsset(), true, callback);
        }
    }

    /**
     * Edit Message
     *
     * @param message the message to be edited
     * @param body    the new message body
     * @param callback save callback
     */

    public void editMessage(@NonNull Message message,
                            @NonNull String body,
                            @Nullable final SaveCallback<Message> callback)
    {
        message.setBody(body);
        this.saveMessage(message, false, callback);
    }

    /**
     * Edit Message
     *
     * @param message the message to be edited
     * @param body    the new message body
     * @param metadata  the new message metadata
     * @param asset  the new message asset
     * @param callback save callback
     */

    public void editMessage(@NonNull Message message,
                            @NonNull String body,
                            @Nullable final JSONObject metadata,
                            @Nullable final Asset asset,
                            @Nullable final SaveCallback<Message> callback)
    {
        message.setBody(body);
        message.setMetadata(metadata);
        message.setAsset(asset);
        this.saveMessage(message, false, callback);
    }


    /**
     * Delete a message
     *
     * @param message the mesqqsage to be deleted
     * @param callback
     */

    public void deleteMessage(@NonNull final Message message, @Nullable final DeleteCallback<Message> callback)
    {
        final MessageOperation operation = this.cacheController.didStartMessageOperation(message,
                message.getConversationId(), MessageOperation.Type.DELETE);

        this.skygear.callLambdaFunction(
                "chat:delete_message",
                new Object[]{ message.getId() },
                new LambdaResponseHandler() {
                    @Override
                    public void onLambdaSuccess(JSONObject result) {
                        if (callback == null) {
                            return;
                        }

                        ChatContainer.this.cacheController.didDeleteMessage(message);
                        ChatContainer.this.cacheController.didCompleteMessageOperation(operation);

                        callback.onSuccess(message);
                    }

                    @Override
                    public void onLambdaFail(Error error) {
                        ChatContainer.this.cacheController.didFailMessageOperation(operation, error);
                        if (callback != null) {
                            callback.onFail(new MessageOperationError(error));
                        }
                    }
                }
        );

    }

    private void saveMessage(final Message message,
                             Boolean isNewMessage,
                             @Nullable final SaveCallback<Message> callback) {
        final MessageOperation operation = this.cacheController.didStartMessageOperation(message,
                message.getConversationId(),
                isNewMessage ? MessageOperation.Type.ADD : MessageOperation.Type.EDIT);

        SaveCallback<Message> wrappedCallback = new SaveCallback<Message>() {
            @Override
            public void onSuccess(@Nullable Message savedMessage) {
                if (savedMessage != null) {
                    ChatContainer.this.cacheController.didSaveMessage(savedMessage);
                    ChatContainer.this.cacheController.didCompleteMessageOperation(operation);
                }

                if (callback != null) {
                    callback.onSuccess(savedMessage);
                }
            }

            @Override
            public void onFail(@NonNull Error error) {
                ChatContainer.this.cacheController.didFailMessageOperation(operation, error);

                if (callback != null) {
                    callback.onFail(error);
                }
            }
        };

        this.skygear.getPublicDatabase().save(
                message.getRecord(),
                new SaveResponseAdapter<Message>(wrappedCallback) {
                    @Override
                    public Message convert(Record record) {
                        return new Message(record);
                    }
                }
        );
    }

    private void saveMessage(final Message message,
                             final Asset asset,
                             final Boolean isNewMessage,
                             @Nullable final SaveCallback<Message> callback) {
        this.skygear.getPublicDatabase().uploadAsset(asset, new AssetPostRequest.ResponseHandler() {
            @Override
            public void onPostSuccess(Asset asset, String response) {
                message.setAsset(asset);
                ChatContainer.this.saveMessage(message, isNewMessage, callback);
            }

            @Override
            public void onPostFail(Asset asset, Error error) {
                Log.w(TAG, "Fail to upload asset: " + error.getMessage());
                ChatContainer.this.saveMessage(message, isNewMessage, callback);
            }
        });
    }

    /* --- Message Operation --- */

    //region Message Operation

    public void fetchOutstandingMessageOperations(@NonNull Conversation conversation,
                                                  @NonNull MessageOperation.Type operationType,
                                                  @Nullable GetCallback<List<MessageOperation>> callback) {
        this.cacheController.fetchMessageOperations(conversation, operationType, callback);
    }

    public void fetchOutstandingMessageOperations(@NonNull Message message,
                                                  @NonNull MessageOperation.Type operationType,
                                                  @Nullable GetCallback<List<MessageOperation>> callback) {
        this.cacheController.fetchMessageOperations(message, operationType, callback);
    }

    public void retryMessageOperation(@NonNull final MessageOperation operation, @NonNull final MessageOperationCallback callback) {
        if (operation.status == MessageOperation.Status.PENDING) {
            Log.w(TAG, String.format("Message operation %s is still pending. Pending operations cannot be cancelled.", operation.operationId));
            return;
        }

        this.cacheController.didCancelMessageOperation(operation);

        switch(operation.type) {
            case ADD:
            case EDIT:
                this.saveMessage(operation.getMessage(),
                        operation.type == MessageOperation.Type.ADD,
                        new SaveCallback<Message>() {
                            @Override
                            public void onSuccess(@Nullable Message object) {
                                if (callback != null) {
                                    callback.onSuccess(operation, object);
                                }
                            }

                            @Override
                            public void onFail(@NonNull Error error) {
                                if (callback != null) {
                                    callback.onFail(error);
                                }
                            }
                        });
                break;
            case DELETE:
                this.deleteMessage(operation.getMessage(),
                        new DeleteCallback<Message>() {
                            @Override
                            public void onSuccess(Message object) {
                                if (callback != null) {
                                    callback.onSuccess(operation, object);
                                }
                            }

                            @Override
                            public void onFail(@NonNull Error error) {
                                if (callback != null) {
                                    callback.onFail(error);
                                }
                            }
                        });
                break;
        }
    }

    public void cancelMessageOperation(@NonNull MessageOperation operation) {
        if (operation.status == MessageOperation.Status.PENDING) {
            Log.w(TAG, String.format("Message operation %s is still pending. Pending operations cannot be cancelled.", operation.operationId));
            return;
        }
        this.cacheController.didCancelMessageOperation(operation);
    }

    //endregion

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

                            callback.onSuccess(receiptList);
                        } catch (JSONException e) {
                            callback.onFail(new JSONError());
                        }
                    }

                    @Override
                    public void onLambdaFail(Error error) {
                        if (callback != null) {
                            callback.onFail(new MessageOperationError(error));
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
            public void onLambdaFail(Error error) {
                Log.i(TAG, "Fail to send typing indicator: " + error.getMessage());
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
        final PubsubContainer pubsub = this.skygear.getPubsub();
        final String conversationId = conversation.getId();

        if (typingSubscription.get(conversationId) == null) {
            getOrCreateUserChannel(new GetCallback<Record>() {
                @Override
                public void onSuccess(@Nullable Record userChannelRecord) {
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
                public void onFail(@NonNull Error error) {
                    Log.w(TAG, "Fail to subscribe typing indicator: " + error.getMessage());
                    if (callback != null) {
                        callback.onSubscriptionFail(error);
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
        final PubsubContainer pubsub = this.skygear.getPubsub();
        String conversationId = conversation.getId();
        Subscription subscription = typingSubscription.get(conversationId);

        if (subscription != null) {
            subscription.detach(pubsub);
            typingSubscription.remove(conversationId);
        }
    }

    /* --- Chat User --- */
    /**
     * Gets all users for the chat plugins.
     *
     * @param callback the callback
     */
    public void getChatUsers(@Nullable final GetChatUsersCallback callback) {
        getChatUsers(null, callback);
    }


    /**
     * Gets users for the chat plugins. If chatUserIds, returns all users.
     * @param chatUserIds user ID to be fetched
     * @param callback the callback
     */
    public void getChatUsers(@Nullable Collection<String> chatUserIds, @Nullable final GetChatUsersCallback callback) {
        Query query = new Query("user");
        if (chatUserIds != null) {
           query.contains("_id", new ArrayList(chatUserIds));
        }
        Database publicDB = this.skygear.getPublicDatabase();
        cacheController.getChatUsers(chatUserIds, callback);

        GetCallback<List<ChatUser>> publicDBCallback = new GetCallback<List<ChatUser>>() {
            @Override
            public void onSuccess(@Nullable List<ChatUser> chatUsers) {
                Map<String, ChatUser> map = new HashMap<>();

                for (ChatUser user : chatUsers) {
                    map.put(user.getId(), user);
                }
                cacheController.didGetChatUsers(chatUsers);
                callback.onSuccess(map);
            }

            @Override
            public void onFail(@NonNull Error error) {
                callback.onFail(error);
            }
        };


        publicDB.query(query, new QueryResponseAdapter<List<ChatUser>>(publicDBCallback) {
            @Override
            public List<ChatUser> convert(Record[] records) {
                ArrayList<ChatUser> chatUsers = new ArrayList<>();
                for (Record record : records) {
                    chatUsers.add(new ChatUser(record));
                }
                return chatUsers;
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
        final PubsubContainer pubsub = this.skygear.getPubsub();
        final String conversationId = conversation.getId();

        if (messageSubscription.get(conversationId) == null) {
            final MessageSubscriptionCallback wrappedCallback = new MessageSubscriptionCallback(conversation) {
                @Override
                public void notify(@NonNull String eventType, @NonNull Message message) {
                    ChatContainer.this.cacheController.handleMessageChange(message, eventType);

                    if (callback != null) {
                        callback.notify(eventType, message);
                    }
                }

                @Override
                public void onSubscriptionFail(@NonNull Error error) {
                    if (callback != null) {
                        callback.onSubscriptionFail(error);
                    }
                }
            };

            getOrCreateUserChannel(new GetCallback<Record>() {
                @Override
                public void onSuccess(@Nullable Record userChannelRecord) {
                    if (userChannelRecord != null) {
                        Subscription subscription = new Subscription(
                                conversationId,
                                (String) userChannelRecord.get("name"),
                                wrappedCallback
                        );
                        subscription.attach(pubsub);
                        messageSubscription.put(conversationId, subscription);
                    }
                }

                @Override
                public void onFail(@NonNull Error error) {
                    Log.w(TAG, "Fail to subscribe conversation message: " + error.getMessage());
                    wrappedCallback.onSubscriptionFail(error);
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
        final PubsubContainer pubsub = this.skygear.getPubsub();
        String conversationId = conversation.getId();
        Subscription subscription = messageSubscription.get(conversationId);

        if (subscription != null) {
            subscription.detach(pubsub);
            messageSubscription.remove(conversationId);
        }
    }

    public void setPubsubListener(@Nullable final PubsubListener listener) {
        this.skygear.getPubsub().setListener(listener);

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
                            callback.onSuccess(records[0]);
                        }
                    } else {
                        createUserChannel(callback);
                    }
                }

                @Override
                public void onQueryError(Error error) {
                    if (callback != null) {
                        callback.onFail(error);
                    }
                }
            });
        } catch (AuthenticationException e) {
            if (callback != null) {
                callback.onFail(new AuthenticationError(e.getMessage()));
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
                        callback.onSuccess(record);
                    }
                }

                @Override
                public void onPartiallySaveSuccess(
                        Map<String, Record> successRecords,
                        Map<String, Error> errors) {

                }

                @Override
                public void onSaveFail(Error error) {
                    if (callback != null) {
                        callback.onFail(error);
                    }
                }
            };

            Database db = this.skygear.getPrivateDatabase();
            db.save(conversation, handler);
        } catch (AuthenticationException e) {
            callback.onFail(new AuthenticationError(e.getMessage()));
        }
    }

}
