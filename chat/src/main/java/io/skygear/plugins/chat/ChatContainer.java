package io.skygear.plugins.chat;


import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
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
