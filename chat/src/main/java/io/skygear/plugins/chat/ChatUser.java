package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Record;

/**
 * The User model for the Chat Plugin.
 */
public class ChatUser {
    // TODO: Implement RecordWrapper when it is available

    static final String NAME_KEY = "name";

    private final Record record;

    /**
     * Instantiates a new Chat User from a Skygear User Record.
     *
     * @param record the record
     */
    ChatUser(final Record record) {
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
     * Gets name.
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return (String) record.get(NAME_KEY);
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
     * Serialization to a JSON Object.
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    /**
     * Deserialization from a JSON Object.
     *
     * @param jsonObject the JSON object
     * @return a chat user
     * @throws JSONException the JSON exception
     */
    public static ChatUser fromJson(JSONObject jsonObject) throws JSONException {
        return new ChatUser(Record.fromJson(jsonObject));
    }
}
