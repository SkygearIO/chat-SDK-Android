package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

final class UnreadSerializer {
    private static final String TYPE_KEY = "$type";
    private static final String TYPE_VAL = "unread";
    private static final String COUNT_KEY = "count";

    /**
     * Serializes a Unread instance
     *
     * @param unread - the Unread
     * @return the json object
     */
    @Nullable
    static JSONObject serialize(Unread unread) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(TYPE_KEY, TYPE_VAL);
            jsonObject.put(COUNT_KEY, unread.getCount());

            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Deserialize a Unread instance from JSON object.
     *
     * @param jsonObject the JSON object
     * @return the Unread instance
     * @throws JSONException the json exception
     */
    static Unread deserialize(JSONObject jsonObject) throws JSONException {
        String typeValue = jsonObject.getString(TYPE_KEY);
        if (typeValue.equals(TYPE_VAL)) {
            return new Unread(jsonObject.getInt(COUNT_KEY));
        }

        throw new JSONException("Invalid $type value: " + typeValue);
    }
}
