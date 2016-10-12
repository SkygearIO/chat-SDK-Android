package io.skygear.plugins.chat.sub;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.plugins.chat.message.Message;
import io.skygear.plugins.chat.callbacks.SubCallback;
import io.skygear.skygear.Container;
import io.skygear.skygear.Pubsub;
import io.skygear.skygear.Record;

final class Sub {
    private static final String LOG_TAG = Sub.class.getSimpleName();

    private final String conversationId;
    private final String channel;
    private final Pubsub.Handler handler;
    private final SubCallback<Message> callback;

    Sub(@NonNull final String conversationId,
        @NonNull final String channel,
        @Nullable final SubCallback<Message> callback) {
        this.conversationId = conversationId;
        this.channel = channel;
        this.callback = callback;
        this.handler = new Pubsub.Handler() {
            @Override
            public void handle(JSONObject data) {
                if (data != null && callback != null) {
                    handleEvent(data);
                }
            }
        };
    }

    void sub(final Container container) {
        Pubsub pubsub = container.getPubsub();
        pubsub.subscribe(channel, this.handler);
    }

    void unSub(final Container container) {
        Pubsub pubsub = container.getPubsub();
        pubsub.unsubscribe(channel, this.handler);
    }

    private void handleEvent(final JSONObject data) {
        try {
            String recordType = data.optString("record_type");
            String evtType = data.optString("event_type");
            JSONObject record = data.optJSONObject("record");
            if (recordType != null && recordType.equals("message") && record != null) {
                Record messageRecord = Record.fromJson(record);

                if (messageRecord != null) {
                    Message message = new Message(messageRecord);
                    String conversationId = message.getConversationId();

                    if (conversationId != null
                            && conversationId.equals(this.conversationId)) {
                        callback.notify(evtType, new Message(messageRecord));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "handleEvent(): " + e.getMessage());
        }
    }
}
