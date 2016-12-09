package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.skygear.skygear.Record;

/**
 * The Conversation model for the Chat Plugin.
 */
public class Conversation {
    // TODO: Implement RecordWrapper when it is available

    static final String TYPE_KEY = "conversation";
    static final String TITLE_KEY = "title";
    static final String ADMIN_IDS_KEY = "admin_ids";
    static final String PARTICIPANT_IDS_KEY = "participant_ids";
    static final String DISTINCT_BY_PARTICIPANTS_KEY = "distinct_by_participant";
    static final String METADATA_KEY = "metadata";

    final Record record;

    private Set<String> adminIds;
    private Set<String> participantIds;

    /**
     * Creates a Compatible Skygear Record
     *
     * @param participantIds the participant ids
     * @param title          the title
     * @param metadata       the metadata
     * @param options        the options
     * @return the record
     */
    static Record newRecord(final Set<String> participantIds,
                            @Nullable final String title,
                            @Nullable final Map<String, Object> metadata,
                            @Nullable final Map<OptionKey, Object> options) {
        Record record = new Record(TYPE_KEY);

        // set participant ids
        JSONArray participantIdArray = new JSONArray(participantIds);
        record.set(PARTICIPANT_IDS_KEY, participantIdArray);

        // set title (allow null)
        if (title != null && title.trim().length() != 0) {
            record.set(TITLE_KEY, title.trim());
        }

        if (metadata != null) {
            record.set(METADATA_KEY, new JSONObject(metadata));
        }

        if (options != null) {
            Object adminIds = options.get(OptionKey.ADMIN_IDS);
            if (adminIds != null) {
                // set admin ids
                JSONArray adminIdArray = new JSONArray((Collection<String>) adminIds);
                record.set(ADMIN_IDS_KEY, adminIdArray);
            }

            // set distinctByParticipants
            Object distinctByParticipants = options.get(OptionKey.DISTINCT_BY_PARTICIPANTS);
            if (distinctByParticipants != null && (boolean)distinctByParticipants) {
                record.set(DISTINCT_BY_PARTICIPANTS_KEY, true);
            }
        }

        return record;
    }

    /**
     * Instantiates a Conversation from a Skygear Record.
     *
     * @param record the record
     */
    Conversation(final Record record) {
        this.record = record;

        JSONArray adminIds = (JSONArray) record.get(ADMIN_IDS_KEY);
        if (adminIds != null) {
            Set<String> ids = new HashSet<>();
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
            Set<String> ids = new HashSet<>();
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
     * Gets id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return record.getId();
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    @Nullable
    public String getTitle() {
        return (String) record.get(TITLE_KEY);
    }

    /**
     * Gets admin ids.
     *
     * @return the admin ids
     */
    @Nullable
    public Set<String> getAdminIds() {
        return adminIds;
    }

    /**
     * Gets participant ids.
     *
     * @return the participant ids
     */
    @Nullable
    public Set<String> getParticipantIds() {
        return participantIds;
    }

    /**
     * Gets metadata.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        Object metadata = this.record.get(METADATA_KEY);
        if (metadata == null) {
            return null;
        }

        if ((metadata instanceof JSONObject)) {
            JSONObject metadataObject = (JSONObject) metadata;
            Iterator<String> keys = metadataObject.keys();
            Map<String, Object> metadataMap = new HashMap<>();

            while (keys.hasNext()) {
                String eachKey = keys.next();
                try {
                    Object eachValue = metadataObject.get(eachKey);
                    metadataMap.put(eachKey, eachValue);
                } catch (JSONException e) {
                    throw new IllegalArgumentException(
                            String.format("Missing value for key %s", eachKey)
                    );
                }
            }

            return metadataMap;
        }

        throw new IllegalArgumentException("Metadata is in incorrect format");
    }

    /**
     * Whether the conversation is distinct by participants.
     *
     * @return the boolean
     */
    public boolean isDistinctByParticipants() {
        return (boolean) record.get(DISTINCT_BY_PARTICIPANTS_KEY);
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
     * Serializes to a JSON Object
     *
     * @return the JSON object
     */
    @Nullable
    public JSONObject toJson() {
        return record.toJson();
    }

    /**
     * Deserializes from a JSON Object
     *
     * @param jsonObject the JSON object
     * @return the conversation
     * @throws JSONException the JSON exception
     */
    public static Conversation fromJson(JSONObject jsonObject) throws JSONException {
        return new Conversation(Record.fromJson(jsonObject));
    }

    /**
     * The Option Key for Conversation Creation.
     */
    public enum OptionKey {
        ADMIN_IDS("admin_ids"),
        DISTINCT_BY_PARTICIPANTS("distinct_by_participant");

        private final String value;

        OptionKey(String value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        String getValue() {
            return this.value;
        }
    }
}
