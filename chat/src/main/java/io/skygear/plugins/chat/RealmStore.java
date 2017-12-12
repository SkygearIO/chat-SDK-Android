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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.RealmModule;

import static io.skygear.plugins.chat.MessageCacheObject.KEY_RECORD_ID;

class RealmStore {

    interface QueryBuilder<T> {
        RealmQuery<T> buildQueryFrom(RealmQuery<T> baseQuery);
    }

    interface ResultCallback<T> {
        void onResultGet(T result);
    }

    private final String name;
    private final boolean inMemory;
    private final boolean async;

    RealmStore(String name, boolean inMemory) {
        this.name = name;
        this.inMemory = inMemory;
        this.async = true;
    }

    RealmStore(String name, boolean inMemory, boolean async) {
        this.name = name;
        this.inMemory = inMemory;
        this.async = async;
    }

    Realm getRealm() {
        RealmConfiguration.Builder configBuilder = new RealmConfiguration.Builder()
                .name(this.name)
                .schemaVersion(1)
                .modules(new SkygearChatModule());

        if (this.inMemory) {
            configBuilder = configBuilder.inMemory();
        }

        return Realm.getInstance(configBuilder.build());
    }

    private void getMessages(final RealmResults<MessageCacheObject> results,
                             final int limit,
                             final ResultCallback<Message[]> callback) {
        OrderedRealmCollectionSnapshot<MessageCacheObject> snapshot = results.createSnapshot();

        int resolvedLimit = limit;
        int size = snapshot.size();
        if (limit == -1 || limit > size) {
            resolvedLimit = size;
        }

        List<Message> messages = new ArrayList<>(resolvedLimit);
        List<MessageCacheObject> faultyCacheObjects = new ArrayList<>();
        for (int i = 0; i < resolvedLimit; i++) {
            MessageCacheObject cacheObject = snapshot.get(i);
            if (cacheObject == null) {
                throw new RuntimeException("Unexpected null object when getting message query result");
            }

            Message message;
            try {
                message = cacheObject.toMessage();
                messages.add(message);
            } catch (Exception e) {
                faultyCacheObjects.add(cacheObject);
            }
        }

        if (faultyCacheObjects.size() > 0) {
            Realm realm = getRealm();
            realm.beginTransaction();

            // clear up faulty cache objects
            for (MessageCacheObject cacheObject : faultyCacheObjects) {
                cacheObject.deleteFromRealm();
            }

            realm.commitTransaction();
        }

        Message[] messageArray = new Message[messages.size()];
        callback.onResultGet(messages.toArray(messageArray));
    }

    void getMessages(@Nonnull final QueryBuilder<MessageCacheObject> queryBuilder,
                     final int limit,
                     @Nonnull final String order,
                     @Nonnull final ResultCallback<Message[]> callback) {
        RealmQuery<MessageCacheObject> query = getRealm().where(MessageCacheObject.class);
        query = queryBuilder.buildQueryFrom(query);
        if (this.async) {
            final RealmResults<MessageCacheObject> results = query.findAllSortedAsync(order, Sort.DESCENDING);
            results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<MessageCacheObject>>() {
                @Override
                public void onChange(RealmResults<MessageCacheObject> messageCacheObjects, @Nullable OrderedCollectionChangeSet changeSet) {
                    results.removeAllChangeListeners();
                    RealmStore.this.getMessages(messageCacheObjects, limit, callback);
                }
            });
        } else {
            final RealmResults<MessageCacheObject> results = query.findAllSorted(order, Sort.DESCENDING);
            this.getMessages(results, limit, callback);
        }
    }

    void getMessageWithID(@Nonnull final String messageID,
                          @Nonnull final ResultCallback<Message> callback) {
        final ResultCallback<Message[]> wrappedCallback = new ResultCallback<Message[]>() {
            @Override
            public void onResultGet(Message[] result) {
                if (result.length == 0) {
                    callback.onResultGet(null);
                } else {
                    callback.onResultGet(result[0]);
                }
            }
        };

        if (this.async) {
            final RealmResults<MessageCacheObject> results = getRealm().where(MessageCacheObject.class).equalTo(KEY_RECORD_ID, messageID).findAllAsync();
            results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<MessageCacheObject>>() {
                @Override
                public void onChange(RealmResults<MessageCacheObject> messageCacheObjects, @Nullable OrderedCollectionChangeSet changeSet) {
                    results.removeAllChangeListeners();
                    RealmStore.this.getMessages(messageCacheObjects, 1, wrappedCallback);
                }
            });
        } else {
            final RealmResults<MessageCacheObject> results = getRealm().where(MessageCacheObject.class).equalTo(KEY_RECORD_ID, messageID).findAll();
            this.getMessages(results, 1, wrappedCallback);
        }
    }

    private void setMessages(final Realm realm, final Message[] messages) {
        List<MessageCacheObject> cacheObjects = new ArrayList<>(messages.length);
        for (Message message : messages) {
            MessageCacheObject cacheObject = new MessageCacheObject(message);
            cacheObjects.add(cacheObject);
        }

        realm.insertOrUpdate(cacheObjects);
    }

    void setMessages(final Message[] messages) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmStore.this.setMessages(realm, messages);
            }
        };

        if (this.async) {
            getRealm().executeTransaction(transaction);
        } else {
            getRealm().executeTransactionAsync(transaction);
        }
    }

    private void deleteMessages(final Realm realm, final Message[] messages) {
        String[] messageIDs = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            messageIDs[i] = messages[i].getId();
        }

        RealmResults<MessageCacheObject> cacheObjects =
                realm.where(MessageCacheObject.class).in(KEY_RECORD_ID, messageIDs).findAll();
        cacheObjects.deleteAllFromRealm();
    }

    void deleteMessages(final Message[] messages) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmStore.this.deleteMessages(realm, messages);
            }
        };

        if (this.async) {
            getRealm().executeTransaction(transaction);
        } else {
            getRealm().executeTransactionAsync(transaction);
        }
    }

    void deleteAll() {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        };

        if (this.async) {
            getRealm().executeTransaction(transaction);
        } else {
            getRealm().executeTransactionAsync(transaction);
        }
    }

    @RealmModule(library = true, classes = {MessageCacheObject.class})
    private static class SkygearChatModule {

    }
}
