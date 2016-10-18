package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Record;

/**
 * Conversation Container for Skygear Chat Plugin.
 */
final class ConversationContainer {
    private static ConversationContainer sharedInstance;

    private final Database publicDB;
    private final String userId;

    /**
     * Gets the Conversation container of Chat Plugin shared within the application.
     *
     * @param container - skygear context
     * @return a Conversation container
     */
    static ConversationContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ConversationContainer(container);
        }

        return sharedInstance;
    }

    private ConversationContainer(final Container container) {
        if (container != null) {
            this.publicDB = container.getPublicDatabase();
            this.userId = container.getCurrentUser().getId();
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
    void create(@Nullable final List<String> participantIds,
                @Nullable final List<String> adminIds,
                @Nullable final String title,
                @Nullable final SaveCallback<Conversation> callback) {
        if (participantIds != null
                && adminIds != null
                && participantIds.size() != 0
                && adminIds.size() != 0) {
            Record record = Conversation.newRecord(participantIds, adminIds, title);
            publicDB.save(record, new SaveResp<Conversation>(callback) {
                @Override
                public Conversation onSuccess(Record record) {
                    return new Conversation(record);
                }
            });
        } else if (callback != null) {
            callback.onFail("participantIds or adminIds can't be null or empty");
        }
    }

    /**
     * Gets all conversations where the user joined.
     *
     * @param callback - GetCallback&lt;List&lt;Conversation&gt;&gt; to handle result conversations
     */
    void getAll(@Nullable final GetCallback<List<Conversation>> callback) {
        publicDB.query(UserConversation.buildQuery(userId),
                new GetResp<List<Conversation>>(callback) {
                    @Override
                    public List<Conversation> onSuccess(Record[] records) {
                        List<Conversation> conversations = new ArrayList<>(records.length);

                        for (Record record : records) {
                            conversations.add(UserConversation.getConversation(record));
                        }

                        return conversations;
                    }
                });
    }

    /**
     * Gets a conversation by id.
     *
     * @param conversationId - the conversation id
     * @param callback - GetCallback&lt;Conversation&gt; to handle result conversation
     */
    void get(@NonNull final String conversationId,
             @Nullable final GetCallback<Conversation> callback) {
        publicDB.query(UserConversation.buildQuery(conversationId, userId),
                new GetResp<Conversation>(callback) {
                    @Override
                    public Conversation onSuccess(Record[] records) {
                        return UserConversation.getConversation(records[0]);
                    }
                });
    }

    /**
     * Update a conversation title by conversation id.
     *
     * @param conversationId - the conversation id
     * @param title - the new title
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    void setTitle(@NonNull final String conversationId,
                  @NonNull final String title,
                  @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.TITLE_KEY, title);

        update(conversationId, map, callback);
    }

    /**
     * Update a conversation admin ids by conversation id.
     *
     * @param conversationId - the conversation id
     * @param adminIds - the new admin ids
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    void setAdminIds(@NonNull final String conversationId,
                            @NonNull final List<String> adminIds,
                            @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[adminIds.size()];
        adminIds.toArray(ids);
        map.put(Conversation.ADMIN_IDS_KEY, ids);

        update(conversationId, map, callback);
    }

    /**
     * Update a conversation participant ids by conversation id.
     *
     * @param conversationId - the conversation id
     * @param participantIds - the new participant ids
     * @param callback - SaveCallback&lt;Conversation&gt; to handle result conversation
     */
    void setParticipantsIds(@NonNull final String conversationId,
                                   @NonNull final List<String> participantIds,
                                   @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[participantIds.size()];
        participantIds.toArray(ids);
        map.put(Conversation.PARTICIPANT_IDS_KEY, ids);

        update(conversationId, map, callback);
    }

    private void update(final String conversationId,
                        final Map<String, Object> map,
                        final SaveCallback<Conversation> callback) {
        GetCallback<Record> getCallback = new GetCallback<Record>() {
            @Override
            public void onSucc(@Nullable Record record) {
                if (record != null) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        record.set(entry.getKey(), entry.getValue());
                    }
                    publicDB.save(record, new SaveResp<Conversation>(callback) {
                        @Override
                        public Conversation onSuccess(Record record) {
                            return new Conversation(record);
                        }
                    });
                }
            }

            @Override
            public void onFail(@Nullable String failReason) {
                if (callback != null) {
                    callback.onFail(failReason);
                }
            }
        };

        publicDB.query(UserConversation.buildQuery(conversationId, userId),
                new GetResp<Record>(getCallback) {
                    @Override
                    public Record onSuccess(Record[] records) {
                        return UserConversation.getConversationRecord(records[0]);
                    }
                });
    }

    /**
     * Delete a conversation by id.
     *
     * @param conversationId - the conversation id
     * @param callback - DeleteOneCallback to handle delete result
     */
    void delete(@NonNull final String conversationId,
                @Nullable final DeleteOneCallback callback) {
        GetCallback<Record> getCallback = new GetCallback<Record>() {
            @Override
            public void onSucc(@Nullable Record record) {
                if (record != null) {
                    publicDB.delete(record, new DeleteResp(callback));
                }
            }

            @Override
            public void onFail(@Nullable String failReason) {
                if (callback != null) {
                    callback.onFail(failReason);
                }
            }
        };

        publicDB.query(UserConversation.buildQuery(conversationId, userId),
                new GetResp<Record>(getCallback) {
                    @Override
                    public Record onSuccess(Record[] records) {
                        return UserConversation.getConversationRecord(records[0]);
                    }
                });
    }

    /**
     * Mark the last read message of a conversation.
     *
     * @param conversationId - the conversation id
     * @param messageId - the last read message id
     */
    void markLastReadMessage(@NonNull final String conversationId,
                             @NonNull final String messageId) {
        GetCallback<Record> getCallback = new GetCallback<Record>() {
            @Override
            public void onSucc(@Nullable Record record) {
                if (record != null) {
                    record.set(UserConversation.LAST_READ_MESSAGE_KEY,
                            Message.newReference(messageId));
                    publicDB.save(record, null);
                }
            }

            @Override
            public void onFail(@Nullable String failReason) {

            }
        };

        publicDB.query(UserConversation.buildQuery(conversationId, userId),
                new GetResp<Record>(getCallback) {
                    @Override
                    public Record onSuccess(Record[] records) {
                        return records[0];
                    }
                });
    }
}
