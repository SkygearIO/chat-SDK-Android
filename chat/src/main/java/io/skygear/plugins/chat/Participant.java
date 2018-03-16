package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Record;

/**
 * The User model for the Chat Plugin.
 */
public class Participant {
    // TODO: Implement RecordWrapper when it is available

    private final Record record;

    /**
     * Instantiates a new Chat User from a Skygear User Record.
     *
     * @param record the record
     */
    public Participant(final Record record) {
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
    public static Participant fromJson(JSONObject jsonObject) throws JSONException {
        return new Participant(Record.fromJson(jsonObject));
    }
}
