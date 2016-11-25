package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

public class Message {
    private static final String TYPE_KEY = "message";
    private static final String BODY_KEY = "body";
    private static final String METADATA_KEY = "metadata";
    private static final String ATTACHMENT_KEY = "attachment";

    private final Record record;

    Message(@NonNull final Record record) {
        this.record = record;
    }

    @NonNull
    public String getId() {
        return record.getId();
    }

    @NonNull
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

    @NonNull
    static Reference newReference(@NonNull final String messageId) {
        return new Reference(TYPE_KEY, messageId);
    }

    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    public static Message fromJson(JSONObject jsonObject) throws JSONException {
        return new Message(Record.fromJson(jsonObject));
    }
}
