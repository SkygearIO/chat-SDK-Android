package io.skygear.plugins.chat;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.skygear.skygear.Record;

public class Conversation {
    private static final String TITLE_KEY = "title";
    private static final String DIRECT_MSG_KEY = "is_direct_message";

    private Record record;
    private List<String> adminIds;
    private List<String> participantIds;

    public Conversation(final Record record) {
        this.record = record;

        JSONArray adminIds = (JSONArray) record.get("admin_ids");
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

        JSONArray participantIds = (JSONArray) record.get("participant_ids");
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

    public String getId() {
        return record.getId();
    }

    public String getTitle() {
        return (String) record.get(TITLE_KEY);
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public boolean isDirectMessage() {
        return (boolean) record.get(DIRECT_MSG_KEY);
    }
}
