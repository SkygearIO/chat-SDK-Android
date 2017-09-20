package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

/**
 * The Conversation model for the Chat Plugin.
 */
public class Conversation {
    // TODO: Implement RecordWrapper when it is available

    static final String TYPE_KEY = "conversation";
    static final String TITLE_KEY = "title";
    static final String LAST_MESSAGE_KEY = "last_message";
    static final String LAST_READ_MESSAGE_KEY = "last_read_message";
    static final String LAST_MESSAGE_REF_KEY = "last_message_ref";
    static final String LAST_READ_MESSAGE_REF_KEY = "last_read_message_ref";
    static final String ADMIN_IDS_KEY = "admin_ids";
    static final String PARTICIPANT_IDS_KEY = "participant_ids";
    static final String DISTINCT_BY_PARTICIPANTS_KEY = "distinct_by_participant";
    static final String METADATA_KEY = "metadata";
    static final String UNREAD_COUNT = "unread_count";

    final Record record;

    private Set<String> adminIds;
    private Set<String> participantIds;
    private Message lastMessage;
    private Message lastReadMessage;
    private static final String TAG = "SkygearChatConversation";

    /**
     * Instantiates a Conversation from a Skygear Record and user information.
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
     * Gets message ids of last message at conversation.
     *
     * @return the the string values of last_message without type
     */
    @Nullable
    public String getLastMessageId() {
        Object obj = this.record.get(LAST_MESSAGE_REF_KEY);
        if (! JSONObject.NULL.equals(obj)) {
            Reference ref = (Reference) obj;
            return ref.getId();
        }
        return null;
    }

    @Nullable
    public String getLastReadMessageId() {
        Object obj = this.record.get(LAST_READ_MESSAGE_REF_KEY);
        if (! JSONObject.NULL.equals(obj)) {
            Reference ref = (Reference) obj;
            return ref.getId();
        }
        return null;
    }

    @Nullable
    private Message getMessage(String key) {
        Object obj = record.get(key);
        if (! JSONObject.NULL.equals(obj)) {
            JSONObject messageRecord = (JSONObject) obj;
            if (messageRecord != null) {
                try {
                    return Message.fromJson(messageRecord);
                } catch (JSONException e) {
                    Log.w(TAG, "Fail parsing last_message", e);
                }
            }
        }
        return null;
    }

    /**
     * Gets the last message
     *
     * @return the last message
     */
    @Nullable
    public Message getLastMessage() {
        if (lastMessage == null) {
            lastMessage = getMessage(LAST_MESSAGE_KEY);
        }
        return lastMessage;
    }

    @Nullable
    public Message getLastReadMessage() {
        if (lastReadMessage == null) {
            lastReadMessage = getMessage(LAST_READ_MESSAGE_KEY);
        }
        return lastReadMessage;
    }

    /**
     * Get Unread Count
     *
     * @return the unread count
     */
    public int getUnreadCount()
    {
        return (int) this.record.get(UNREAD_COUNT);
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
