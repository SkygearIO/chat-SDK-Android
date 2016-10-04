package io.skygear.plugins.chat;


import java.util.List;

public class Conversation {
    private String id;
    private String title;
    private List<String> adminIds;
    private List<String> participantIds;
    private boolean isDirectMessage;

    public static class Builder {
        private String id;
        private String title;
        private List<String> adminIds;
        private List<String> participantIds;
        private boolean isDirectMessage;

        public Builder() {
            super();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder adminIds(List<String> adminIds) {
            this.adminIds = adminIds;
            return this;
        }

        public Builder participantIds(List<String> participantIds) {
            this.participantIds = participantIds;
            return this;
        }

        public Builder directMessage(boolean directMessage) {
            isDirectMessage = directMessage;
            return this;
        }

        public Conversation build() {
            return new Conversation(this);
        }
    }

    private Conversation(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.adminIds = builder.adminIds;
        this.participantIds = builder.participantIds;
        this.isDirectMessage = builder.isDirectMessage;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public boolean isDirectMessage() {
        return isDirectMessage;
    }
}
