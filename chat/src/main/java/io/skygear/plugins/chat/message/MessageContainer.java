package io.skygear.plugins.chat.message;


import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.skygear.plugins.chat.SaveResp;
import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.plugins.chat.callbacks.SaveCallback;
import io.skygear.plugins.chat.utils.DateUtils;
import io.skygear.plugins.chat.utils.StringUtils;
import io.skygear.skygear.Asset;
import io.skygear.skygear.AssetPostRequest;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

public final class MessageContainer {
    private static final String LOG_TAG = MessageContainer.class.getSimpleName();
    private static final int LIMIT = 50; // default value

    private static MessageContainer sharedInstance;
    private Container container;

    public static MessageContainer getInstance(final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new MessageContainer(container);
        }

        return sharedInstance;
    }

    private MessageContainer(final Container container) {
        this.container = container;
    }

    public void getAll(final String conversationId,
                       final int limit,
                       @Nullable final Date before,
                       @Nullable final GetCallback<List<Message>> callback) {
        if (StringUtils.isNotEmpty(conversationId)) {
            int limitCount = limit;
            String beforeTimeISO8601 = DateUtils.toISO8601(before != null ? before : new Date());

            if (limitCount <= 0) {
                limitCount = LIMIT;
            }

            Object[] args = new Object[]{conversationId, limitCount, beforeTimeISO8601};
            container.callLambdaFunction("chat:get_messages", args, new LambdaResponseHandler() {
                @Override
                public void onLambdaSuccess(JSONObject result) {
                    List<Message> messages = buildMessages(result.optJSONArray("results"));
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
        } else if (callback != null) {
            callback.onFail("Conversation ID can't be null or empty");
        }
    }

    public void send(final String conversationId,
                     @Nullable final String body,
                     @Nullable final Asset asset,
                     @Nullable final JSONObject metadata,
                     @Nullable final SaveCallback<Message> callback) {
        if (StringUtils.isNotEmpty(conversationId)
                && !(StringUtils.isEmpty(body) && asset == null && metadata == null)) {
            Record record = new Record("message");
            Reference reference = new Reference("conversation", conversationId);
            record.set("conversation_id", reference);
            if (body != null) {
                record.set("body", body);
            }
            if (metadata != null) {
                record.set("metadata", metadata);
            }

            if (asset == null) {
                save(record, callback);
            } else {
                upload(asset, record, callback);
            }
        } else {
            if (callback != null) {
                callback.onFail("Please provide either body, asset or metadata");
            }
        }
    }

    private void upload(final Asset asset,
                        final Record message,
                        @Nullable final SaveCallback<Message> callback) {
        container.uploadAsset(asset, new AssetPostRequest.ResponseHandler() {
            @Override
            public void onPostSuccess(Asset asset, String response) {
                message.set("attachment", asset);
                save(message, callback);
            }

            @Override
            public void onPostFail(Asset asset, String reason) {
                save(message, callback);
            }
        });
    }

    private void save(final Record message,
                      @Nullable final SaveCallback<Message> callback) {
        Database publicDB = container.getPublicDatabase();
        publicDB.save(message, new SaveResp<Message>(callback) {
            @Override
            public Message onSuccess(Record record) {
                return new Message(record);
            }
        });
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
}
