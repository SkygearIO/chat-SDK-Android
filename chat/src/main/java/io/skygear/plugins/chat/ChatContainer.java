package io.skygear.plugins.chat;


import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;

public final class ChatContainer {
    private static final String LOG_TAG = ChatContainer.class.getSimpleName();

    private static ChatContainer sharedInstance;

    private Container container;

    public static ChatContainer getInstance(final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ChatContainer(container);
        }

        return sharedInstance;
    }

    private ChatContainer(final Container container) {
        this.container = container;
    }

    //----------------------------------------------------------------------------------------------

    public void createConversation(final List<String> participantIds,
                                   final List<String> adminIds,
                                   final String title,
                                   final CreateCallback<Conversation> callback) {
        if (participantIds != null && adminIds != null
                && participantIds.size() != 0
                && adminIds.size() != 0) {
            Record conversation = new Record("conversation");
            String[] ids = new String[participantIds.size()];
            participantIds.toArray(ids);
            conversation.set("participant_ids", ids);
            ids = new String[adminIds.size()];
            adminIds.toArray(ids);
            conversation.set("admin_ids", ids);
            conversation.set("title", title);
            conversation.set("is_direct_message", participantIds.size() < 3);

            RecordSaveResponseHandler handler = new RecordSaveResponseHandler() {
                @Override
                public void onSaveSuccess(Record[] records) {
                    Record record = records[0];
                    Conversation conversation = new Conversation(record);
                    if (callback != null) {
                        callback.done(conversation, null);
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
                        callback.done(null, reason);
                    }
                }
            };

            Database publicDB = container.getPublicDatabase();
            publicDB.save(conversation, handler);
        } else {
            if (callback != null) {
                callback.done(null, "participantIds or adminIds can't be null or empty");
            }
        }
    }

    public void getConversations(final GetCallback<Conversation> callback) {
        Query query = new Query("user_conversation")
                .equalTo("user", container.getCurrentUser().getId())
                .transientInclude("user")
                .transientInclude("conversation");
        Database publicDB = container.getPublicDatabase();

        publicDB.query(query, new RecordQueryResponseHandler() {
            @Override
            public void onQuerySuccess(Record[] records) {
                List<Conversation> conversations = new ArrayList<>(records.length);

                for (Record record : records) {
                    Map<String, Record> includeMap = record.getTransient();
                    Record conversationRecord = includeMap.get("conversation");
                    Conversation conversation = new Conversation(conversationRecord);
                    conversations.add(conversation);
                }

                if (callback != null) {
                    callback.done(conversations, null);
                }
            }

            @Override
            public void onQueryError(String reason) {
                if (callback != null) {
                    callback.done(null, reason);
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    public void getMessages(final String conversationId,
                            final int limit,
                            @Nullable final Date before,
                            final GetCallback<Message> callback) {
        final int LIMIT = 50; // default value

        if (conversationId != null && conversationId.length() != 0) {
            int limitCount = limit;
            String beforeTimeISO8601 = DateUtil.toISO8601(before != null ? before : new Date());

            if (limitCount <= 0) {
                limitCount = LIMIT;
            }

            Object[] args = new Object[]{conversationId, limitCount, beforeTimeISO8601};
            container.callLambdaFunction("chat:get_messages", args, new LambdaResponseHandler() {
                @Override
                public void onLambdaSuccess(JSONObject result) {
                    List<Message> messages = buildMessages(result.optJSONArray("results"));
                    callback.done(messages, null);
                }

                @Override
                public void onLambdaFail(String reason) {
                    callback.done(null, reason);
                }
            });
        }
    }

    private List<Message> buildMessages(final JSONArray results) {
        List<Message> messages = null;

        if (results != null) {
            messages = new ArrayList<>(results.length());

            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject object = results.getJSONObject(i);
                    Message message = buildMessage(object);
                    if (message != null) {
                        messages.add(message);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "buildMessages: " + e.getMessage());
                }
            }
        }

        return messages;
    }

    private Message buildMessage(final JSONObject object) {
        Message message = null;

        try {
            Record record = Record.fromJson(object);
            message = new Message(record);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "buildMessage: " + e.getMessage());
        }

        return message;
    }

    //----------------------------------------------------------------------------------------------

    public void getChatUsers(final GetCallback<ChatUser> callback) {
        Query query = new Query("user");
        Database publicDB = container.getPublicDatabase();
        publicDB.query(query, new RecordQueryResponseHandler() {
            @Override
            public void onQuerySuccess(Record[] records) {
                List<ChatUser> users = new ArrayList<>(records.length);

                for (Record record : records) {
                    String id = record.getId();
                    String name = (String) record.get("name");
                    if (id != null) {
                        users.add(new ChatUser(id, name));
                    }
                }

                if (callback != null) {
                    callback.done(users, null);
                }
            }

            @Override
            public void onQueryError(String reason) {
                if (callback != null) {
                    callback.done(null, reason);
                }
            }
        });
    }
}
