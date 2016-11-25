package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Record;

public class ChatUser {
    private static final String NAME_KEY = "name";

    private final Record record;

    ChatUser(final Record record) {
        this.record = record;
    }

    @NonNull
    public String getId() {
        return record.getId();
    }

    @Nullable
    public String getName() {
        return (String) record.get(NAME_KEY);
    }

    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    public static ChatUser fromJson(JSONObject jsonObject) throws JSONException {
        return new ChatUser(Record.fromJson(jsonObject));
    }
}
