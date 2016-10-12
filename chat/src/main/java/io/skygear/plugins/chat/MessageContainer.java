package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.skygear.skygear.Asset;
import io.skygear.skygear.AssetPostRequest;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.LambdaResponseHandler;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

/**
 * Message Container for Skygear Chat Plugin.
 */
public final class MessageContainer {
    private static final String LOG_TAG = MessageContainer.class.getSimpleName();
    private static final int LIMIT = 50; // default value

    private static MessageContainer sharedInstance;
    private final Container container;

    /**
     * Gets the Message container of Chat Plugin shared within the application.
     *
     * @param container - skygear context
     * @return a Message container
     */
    public static MessageContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new MessageContainer(container);
        }

        return sharedInstance;
    }

    private MessageContainer(final Container container) {
        this.container = container;
    }

    /**
     * Gets all messages of a conversation.
     *
     * @param conversationId - the conversation id
     * @param limit - the limit of number of messages, default value is 50
     * @param before - get the messages before the Date instance
     * @param callback - GetCallback<List<Message>> to handle messages
     */
    public void getAll(@NonNull final String conversationId,
                       final int limit,
                       @Nullable final Date before,
                       @Nullable final GetCallback<List<Message>> callback) {
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
    }

    /**
     * Send a message to a conversation.
     *
     * @param conversationId - the conversation id
     * @param body - the message body
     * @param asset - the message asset
     * @param metadata - the message metadata
     * @param callback - SaveCallback<Message> to handle send result
     *
     * Either body, asset or metadata can't be null
     */
    public void send(@NonNull final String conversationId,
                     @Nullable final String body,
                     @Nullable final Asset asset,
                     @Nullable final JSONObject metadata,
                     @Nullable final SaveCallback<Message> callback) {
        if (!StringUtils.isEmpty(body) || asset != null || metadata != null) {
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

    private Message buildMessage(final JSONObject object) throws JSONException {
        Record record = Record.fromJson(object);
        return new Message(record);
    }
}
