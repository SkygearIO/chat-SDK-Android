package io.skygear.plugins.chat;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Skygear Chat Plugin - Unread.
 */
public class Unread {
    private final int count;

    /**
     * Instantiates a new Unread.
     *
     * @param count the unread count
     */
    Unread(final int count) {
        this.count = count;
    }

    /**
     * Gets the unread count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Serializes the message.
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return UnreadSerializer.serialize(this);
    }

    /**
     * Deserializes the message.
     *
     * @param jsonObject the json object
     * @return the message
     * @throws JSONException the json exception
     */
    public static Unread fromJson(JSONObject jsonObject) throws JSONException {
        return UnreadSerializer.deserialize(jsonObject);
    }
}
