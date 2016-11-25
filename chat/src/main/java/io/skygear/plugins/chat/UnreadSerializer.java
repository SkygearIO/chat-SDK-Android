package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

final class UnreadSerializer {
    private static final String TYPE_KEY = "$type";
    private static final String TYPE_VAL = "unread";
    private static final String COUNT_KEY = "count";

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

    static Unread deserialize(JSONObject jsonObject) throws JSONException {
        String typeValue = jsonObject.getString(TYPE_KEY);
        if (typeValue.equals(TYPE_VAL)) {
            return new Unread(jsonObject.getInt(COUNT_KEY));
        }

        throw new JSONException("Invalid $type value: " + typeValue);
    }
}
