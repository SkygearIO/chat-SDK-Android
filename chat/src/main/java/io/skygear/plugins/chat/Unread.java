package io.skygear.plugins.chat;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Unread {
    private final int count;

    Unread(final int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public JSONObject toJson() {
        return UnreadSerializer.serialize(this);
    }

    public static Unread fromJson(JSONObject jsonObject) throws JSONException {
        return UnreadSerializer.deserialize(jsonObject);
    }
}
