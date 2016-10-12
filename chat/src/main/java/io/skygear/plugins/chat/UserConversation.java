package io.skygear.plugins.chat;


import java.util.Map;

import io.skygear.skygear.Query;
import io.skygear.skygear.Record;

final class UserConversation {
    private static final String TYPE_KEY = "user_conversation";
    private static final String CONVERSATION_KEY = "conversation";
    private static final String USER_KEY = "user";
    static final String LAST_READ_MESSAGE_KEY = "last_read_message";

    static Conversation getConversation(Record record) {
        Map<String, Object> includeMap = record.getTransient();
        Record conversationRecord = (Record) includeMap.get(CONVERSATION_KEY);
        return new Conversation(conversationRecord);
    }

    static Record getConversationRecord(Record record) {
        Map<String, Object> includeMap = record.getTransient();
        return (Record) includeMap.get(CONVERSATION_KEY);
    }

    static Query buildQuery(final String userId) {
        return new Query(TYPE_KEY)
                .equalTo(USER_KEY, userId)
                .transientInclude(CONVERSATION_KEY)
                .transientInclude(USER_KEY);
    }

    static Query buildQuery(final String conversationId, final String userId) {
        return new Query(TYPE_KEY)
                .equalTo(USER_KEY, userId)
                .equalTo(CONVERSATION_KEY, conversationId)
                .transientInclude(CONVERSATION_KEY);
    }
}
