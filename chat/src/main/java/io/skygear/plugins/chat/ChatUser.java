package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Record;

/**
 * The Skygear Chat Plugin - ChatUser.
 */
public class ChatUser {
    private static final String NAME_KEY = "name";

    private final Record record;

    ChatUser(final Record record) {
        this.record = record;
    }

    /**
     * Gets the chat user id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return record.getId();
    }

    /**
     * Gets the chat user name.
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return (String) record.get(NAME_KEY);
    }

    /**
     * Serializes the ChatUser.
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    /**
     * Deserializes the ChatUser.
     *
     * @param jsonObject the json object
     * @return the ChatUser
     * @throws JSONException the json exception
     */
    public static ChatUser fromJson(JSONObject jsonObject) throws JSONException {
        return new ChatUser(Record.fromJson(jsonObject));
    }
}
