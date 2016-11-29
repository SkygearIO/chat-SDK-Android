package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

public class Message {
    static final String TYPE_KEY = "message";
    static final String BODY_KEY = "body";
    static final String METADATA_KEY = "metadata";
    static final String ATTACHMENT_KEY = "attachment";
    static final String CONVERSATION_STATUS_KEY = "conversation_status";

    final Record record;

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
    public Status getStatus() {
        return Status.fromName((String) this.record.get(CONVERSATION_STATUS_KEY));
    }

    @NonNull
    public Date getCreatedTime() {
        return this.record.getCreatedAt();
    }

    @NonNull
    public Date getUpdatedTime() {
        return this.record.getUpdatedAt();
    }

    @Nullable
    public Asset getAsset() {
        return (Asset) record.get(ATTACHMENT_KEY);
    }

    @NonNull
    static Reference newReference(@NonNull final String messageId) {
        return new Reference(TYPE_KEY, messageId);
    }

    @NonNull
    static Reference newReference(@NonNull final Message message) {
        return Message.newReference(message.getId());
    }

    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    public static Message fromJson(JSONObject jsonObject) throws JSONException {
        return new Message(Record.fromJson(jsonObject));
    }

    public enum Status {
        DELIVERED("delivered"), SOME_READ("some_read"), ALL_READ("all_read");

        private final String name;

        public String getName() {
            return name;
        }

        Status(String name) {
            this.name = name;
        }

        @Nullable
        static Status fromName(String name) {
            Status status = null;
            for (Status eachStatus : Status.values()) {
                if (eachStatus.getName().equals(name)) {
                    status = eachStatus;
                    break;
                }
            }
            return status;
        }
    }
}
