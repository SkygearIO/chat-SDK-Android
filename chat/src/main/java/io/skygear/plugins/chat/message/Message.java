package io.skygear.plugins.chat.message;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

/**
 * The Skygear Chat Plugin - Message.
 */
public class Message {
    private static final String TYPE_KEY = "message";
    private static final String BODY_KEY = "body";
    private static final String METADATA_KEY = "metadata";
    private static final String ATTACHMENT_KEY = "attachment";

    private final Record record;

    /**
     * Default constructor of Message
     *
     * @param record - skygear Message record
     */
    public Message(@NonNull final Record record) {
        this.record = record;
    }

    /**
     * Gets the message id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return record.getId();
    }

    /**
     * Gets the conversation id where the message belongs to.
     *
     * @return the id
     */
    @NonNull
    public String getConversationId() {
        Reference reference = (Reference) record.get("conversation_id");
        return reference.getId();
    }

    /**
     * Gets the message body.
     *
     * @return the body
     */
    @Nullable
    public String getBody() {
        return (String)record.get(BODY_KEY);
    }

    /**
     * Gets the message meta-data.
     *
     * @return the meta-data
     */
    @Nullable
    public JSONObject getMetaData() {
        if (!record.get(METADATA_KEY).equals(JSONObject.NULL)) {
            return (JSONObject) record.get(METADATA_KEY);
        } else {
            return null;
        }
    }

    /**
     * Gets the message asset.
     *
     * @return the asset
     */
    @Nullable
    public Asset getAsset() {
        return (Asset) record.get(ATTACHMENT_KEY);
    }

    /**
     * Create the message reference.
     *
     * @param messageId - the id of the reference message
     * @return the message reference
     */
    @NonNull
    public static Reference newReference(@NonNull final String messageId) {
        return new Reference(TYPE_KEY, messageId);
    }
}
