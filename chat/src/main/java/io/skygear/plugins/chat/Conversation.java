package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.skygear.skygear.Record;

/**
 * The Skygear Chat Plugin - Conversation.
 */
public class Conversation {
    private static final String TYPE_KEY = "conversation";
    static final String TITLE_KEY = "title";
    static final String DIRECT_MSG_KEY = "is_direct_message";
    static final String ADMIN_IDS_KEY = "admin_ids";
    static final String PARTICIPANT_IDS_KEY = "participant_ids";

    private final Record record;
    private List<String> adminIds;
    private List<String> participantIds;

    static Record newRecord(final List<String> participantIds,
                            final List<String> adminIds,
                            @Nullable final String title) {
        Record record = new Record(TYPE_KEY);

        // set participant ids
        String[] ids = new String[participantIds.size()];
        participantIds.toArray(ids);
        record.set(PARTICIPANT_IDS_KEY, ids);

        // set admin ids
        ids = new String[adminIds.size()];
        adminIds.toArray(ids);
        record.set(ADMIN_IDS_KEY, ids);

        // set title (allow null)
        if (title != null && title.trim().length() != 0) {
            record.set(TITLE_KEY, title.trim());
        }

        // is_direct_message
        record.set(DIRECT_MSG_KEY, participantIds.size() < 3);

        return record;
    }

    Conversation(final Record record) {
        this.record = record;

        JSONArray adminIds = (JSONArray) record.get(ADMIN_IDS_KEY);
        if (adminIds != null) {
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < adminIds.length(); i++) {
                String id = adminIds.optString(i);
                if (id != null) {
                    ids.add(id);
                }
            }
            this.adminIds = ids;
        }

        JSONArray participantIds = (JSONArray) record.get(PARTICIPANT_IDS_KEY);
        if (participantIds != null) {
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < participantIds.length(); i++) {
                String id = participantIds.optString(i);
                if (id != null) {
                    ids.add(id);
                }
            }
            this.participantIds = ids;
        }
    }

    /**
     * Gets the conversation id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return record.getId();
    }

    /**
     * Gets the conversation title.
     *
     * @return the title
     */
    @Nullable
    public String getTitle() {
        return (String) record.get(TITLE_KEY);
    }

    /**
     * Gets the conversation admin ids.
     *
     * @return the admin ids
     */
    @Nullable
    public List<String> getAdminIds() {
        return adminIds;
    }

    /**
     * Gets the conversation participant ids.
     *
     * @return the participant ids
     */
    @Nullable
    public List<String> getParticipantIds() {
        return participantIds;
    }

    /**
     * Weather the conversion is 1-1 direct conversation of not
     *
     * @return is direct message of not
     */
    public boolean isDirectMessage() {
        return (boolean) record.get(DIRECT_MSG_KEY);
    }

    /**
     * Serializes the conversation.
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    /**
     * Deserializes the message.
     *
     * @param jsonObject the json object
     * @return the message
     * @throws JSONException the json exception
     */
    public static Conversation fromJson(JSONObject jsonObject) throws JSONException {
        return new Conversation(Record.fromJson(jsonObject));
    }
}
