package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

/**
 * The Message model for Chat Plugin.
 */
public class Message {
    // TODO: Implement RecordWrapper when it is available

    static final String TYPE_KEY = "message";
    static final String BODY_KEY = "body";
    static final String METADATA_KEY = "metadata";
    static final String ATTACHMENT_KEY = "attachment";
    static final String MESSAGE_STATUS_KEY = "message_status";
    static final String SEQ_KEY = "seq";

    final Record record;

    /**
     * Transient fields that are not saved to the server
     */
    Date sendDate;

    /**
     * Instantiates a new Message with new Skygear Record.
     */
    public Message() {
        this.record = new Record("message");
    }

    /**
     * Instantiates a new Message from a Skygear Record.
     *
     * @param record the record
     */
    public Message(@NonNull final Record record) {
        this.record = record;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return record.getId();
    }

    /**
     * Gets conversation id.
     *
     * @return the conversation id
     */
    @NonNull
    public String getConversationId() {
        Reference reference = (Reference) record.get("conversation");
        return reference.getId();
    }

    /**
     * Sets body.
     *
     */
    public void setBody(String body) {
        record.set(BODY_KEY, body);
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    @Nullable
    public String getBody() {
        return (String)record.get(BODY_KEY);
    }

    /**
     * Sets metadata.
     *
     * @param metadata metadata
     */
    @Nullable
    public void setMetadata(JSONObject metadata) {
        record.set(METADATA_KEY, metadata);
    }

    /**
     * Gets metadata.
     *
     * @return the metadata
     */
    @Nullable
    public JSONObject getMetadata() {
        if (!record.get(METADATA_KEY).equals(JSONObject.NULL)) {
            return (JSONObject) record.get(METADATA_KEY);
        } else {
            return null;
        }
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    @Nullable
    public Status getStatus() {
        return Status.fromName((String) this.record.get(MESSAGE_STATUS_KEY));
    }

    /**
     * Gets created time.
     *
     * @return the created time
     */
    @NonNull
    public Date getCreatedTime() {
        return this.record.getCreatedAt();
    }

    /**
     * Gets updated time.
     *
     * @return the updated time
     */
    @NonNull
    public Date getUpdatedTime() {
        return this.record.getUpdatedAt();
    }

    /**
     * Sets asset.
     *
     * @param asset asset
     */
    @Nullable
    public void setAsset(Asset asset) {
        record.set(ATTACHMENT_KEY, asset);
    }

    /**
     * Gets asset.
     *
     * @return the asset
     */
    @Nullable
    public Asset getAsset() {
        return (Asset) record.get(ATTACHMENT_KEY);
    }

    /**
     * Gets if message is deleted.
     *
     * @return if message is deleted
     */
    public boolean isDeleted() {
        return (boolean) this.record.get("deleted");
    }

    /**
     * Gets message send date.
     *
     * @return message send date
     */
    public Date getSendDate() {
        return this.sendDate;
    }

    /**
     * Gets seq.
     *
     * @return the seq
     */
    @NonNull
    public int getSeq() {
        Object value = record.get(SEQ_KEY);
        return value == null ? 0 : (Integer) value;
    }

    /**
     * Gets record.
     *
     * @return the Skygear record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Creates a Skygear Reference by message ID
     *
     * @param messageId the message id
     * @return the reference
     */
    @NonNull
    static Reference newReference(@NonNull final String messageId) {
        return new Reference(TYPE_KEY, messageId);
    }

    /**
     * Creates a Skygear Reference by a message
     *
     * @param message the message
     * @return the reference
     */
    @NonNull
    static Reference newReference(@NonNull final Message message) {
        return Message.newReference(message.getId());
    }

    /**
     * Serializes to a JSON Object
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    /**
     * Deserialization from a JSON Object
     *
     * @param jsonObject the JSON object
     * @return the message
     * @throws JSONException the JSON exception
     */
    public static Message fromJson(JSONObject jsonObject) throws JSONException {
        return new Message(Record.fromJson(jsonObject));
    }

    /**
     * The Message Status.
     */
    public enum Status {
        DELIVERED("delivered"),
        SOME_READ("some_read"),
        ALL_READ("all_read");

        private final String name;

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        Status(String name) {
            this.name = name;
        }

        /**
         * Creates a status from a name
         *
         * @param name the name
         * @return the status
         */
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
