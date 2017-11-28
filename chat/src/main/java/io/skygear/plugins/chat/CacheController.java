/*
 * Copyright 2017 Oursky Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.RealmQuery;

/**
 * The cache controller that contains the logic of updating cache store when
 * calling chat container api and receiving response.
 */
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

    void getMessages(@NonNull final Conversation conversation,
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
