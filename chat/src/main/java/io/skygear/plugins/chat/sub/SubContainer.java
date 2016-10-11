package io.skygear.plugins.chat.sub;


import android.support.annotation.Nullable;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.plugins.chat.callbacks.SubCallback;
import io.skygear.plugins.chat.message.Message;
import io.skygear.skygear.AuthenticationException;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;

public final class SubContainer {
    private static final String LOG_TAG = SubContainer.class.getSimpleName();

    private static SubContainer sharedInstance;
    private Container container;
    private Map<String, Sub> subs = new HashMap<>();

    public static SubContainer getInstance(final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new SubContainer(container);
        }

        return sharedInstance;
    }

    private SubContainer(final Container container) {
        this.container = container;
    }

    public void sub(final String conversationId,
                    final SubCallback<Message> callback) {
        Sub sub = subs.get(conversationId);

        if (sub == null) {
            getOrCreateUserChannel(new GetCallback<Record>() {
                @Override
                public void onSucc(@Nullable Record record) {
                    if (record != null) {
                        Sub sub = new Sub(conversationId, (String) record.get("name"),
                                        callback);
                        sub.sub(container);
                        subs.put(conversationId, sub);
                    }
                }

                @Override
                public void onFail(@Nullable String failReason) {

                }
            });
        } else {
            throw new InvalidParameterException("Don't subscribe a conversation more than once");
        }
    }

    public void unSub(final String conversationId) {
        Sub sub = subs.get(conversationId);

        if (sub != null) {
            sub.unSub(container);
            subs.remove(conversationId);
        } else {
            throw new InvalidParameterException("Don't un-subscribe a conversation more than once");
        }
    }

    private void getOrCreateUserChannel(@Nullable final GetCallback<Record> callback) {
        try {
            Query query = new Query("user_channel");
            Database privateDatabase = container.getPrivateDatabase();
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

            Database db = container.getPrivateDatabase();
            db.save(conversation, handler);
        } catch (AuthenticationException e) {
            callback.onFail(e.getMessage());
        }
    }
}
