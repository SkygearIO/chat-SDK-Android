package io.skygear.plugins.chat.message;


import android.support.annotation.Nullable;

import org.json.JSONObject;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

public class Message {
    private static final String TYPE_KEY = "message";

    private static final String LOG_TAG = Message.class.getSimpleName();
    private static final String BODY_KEY = "body";
    private static final String METADATA_KEY = "metadata";
    private static final String ATTACHMENT_KEY = "attachment";

    private Record record;

    public Message(final Record record) {
        this.record = record;
    }

    public String getId() {
        return record.getId();
    }

    public String getConversationId() {
        Reference reference = (Reference) record.get("conversation_id");
        return reference.getId();
    }

    @Nullable
    public String getBody() {
        return (String)record.get(BODY_KEY);
    }

    @Nullable
    public JSONObject getMetaData() {
        if (!record.get(METADATA_KEY).equals(JSONObject.NULL)) {
            return (JSONObject) record.get(METADATA_KEY);
        } else {
            return null;
        }
    }

    @Nullable
    public Asset getAsset() {
        return (Asset) record.get(ATTACHMENT_KEY);
    }

    public static Reference newReference(final String messageId) {
        return new Reference(TYPE_KEY, messageId);
    }
}
