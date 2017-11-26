package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.RealmQuery;

class CacheController {
    private static CacheController sharedInstance;

    static CacheController getInstance() {
        if (sharedInstance == null) {
            RealmStore store = new RealmStore("SKYChatCache", false);
            sharedInstance = new CacheController(store);
        }

        return sharedInstance;
    }

    RealmStore store;

    CacheController(RealmStore store) {
        this.store = store;
    }

    public void getMessages(@NonNull final Conversation conversation,
                            final int limit,
                            @Nullable final Date before,
                            @Nullable final String order,
                            @Nullable final GetCallback<List<Message>> callback) {
        RealmStore.QueryBuilder<MessageCacheObject> queryBuilder = new RealmStore.QueryBuilder<MessageCacheObject>() {
            @Override
            public RealmQuery<MessageCacheObject> buildQueryFrom(RealmQuery<MessageCacheObject> baseQuery) {
                RealmQuery<MessageCacheObject>  query = baseQuery
                        .equalTo("conversationID", conversation.getId())
                        .equalTo("deleted", false)
                        .beginGroup()
                            .beginGroup()
                                .equalTo("alreadySyncToServer", true)
                                .equalTo("fail", false)
                            .endGroup()
                            .or()
                            .isNull("sendDate")
                        .endGroup();

                if (before != null) {
                    query.lessThan("creationDate", before);
                }

                return query;
            }
        };

        String resolvedOrder = order;
        if (resolvedOrder != null && resolvedOrder.equalsIgnoreCase("edited_at")) {
            resolvedOrder = "editionDate";
        } else {
            resolvedOrder = "creationDate";
        }

        if (callback != null) {
            Message[] messages = this.store.getMessages(queryBuilder, limit, resolvedOrder);
            callback.onSucc(Arrays.asList(messages));
        }
    }

    void didGetMessages(Message[] messages, Message[] deletedMessages) {
        this.store.setMessages(messages);

        // soft delete
        // so update the messages
        this.store.setMessages(deletedMessages);
    }
}
