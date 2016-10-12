package io.skygear.plugins.chat.conversation;


import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.skygear.plugins.chat.resps.DeleteResp;
import io.skygear.plugins.chat.resps.GetResp;
import io.skygear.plugins.chat.resps.SaveResp;
import io.skygear.plugins.chat.callbacks.DeleteOneCallback;
import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.plugins.chat.callbacks.SaveCallback;
import io.skygear.plugins.chat.message.Message;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Record;


public final class ConversationContainer {
    private static ConversationContainer sharedInstance;

    private Database publicDB;
    private String userId;

    public static ConversationContainer getInstance(final Container container) {
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

    public void create(@Nullable final List<String> participantIds,
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

    public void getAll(@Nullable final GetCallback<List<Conversation>> callback) {
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

    public void get(final String conversationId,
                    @Nullable final GetCallback<Conversation> callback) {
        publicDB.query(UserConversation.buildQuery(conversationId, userId),
                new GetResp<Conversation>(callback) {
                    @Override
                    public Conversation onSuccess(Record[] records) {
                        return UserConversation.getConversation(records[0]);
                    }
                });
    }

    public void update(final String conversationId,
                       final String title,
                       @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put(Conversation.TITLE_KEY, title);

        update(conversationId, map, callback);
    }

    public void setAdminIds(final String conversationId,
                            final List<String> adminIds,
                            @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[adminIds.size()];
        adminIds.toArray(ids);
        map.put(Conversation.ADMIN_IDS_KEY, ids);

        update(conversationId, map, callback);
    }

    public void setParticipantsIds(final String conversationId,
                                   final List<String> participantIds,
                                   @Nullable final SaveCallback<Conversation> callback) {
        Map<String, Object> map = new HashMap<>();
        String[] ids = new String[participantIds.size()];
        participantIds.toArray(ids);
        map.put(Conversation.PARTICIPANT_IDS_KEY, ids);
        map.put(Conversation.DIRECT_MSG_KEY, participantIds.size() < 3);

        update(conversationId, map, callback);
    }

    private void update(final String conversationId,
                        final Map<String, Object> map,
                        @Nullable final SaveCallback<Conversation> callback) {
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

    public void delete(final String conversationId,
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

    public void markLastReadMessage(final String conversationId,
                                    final String messageId) {
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
