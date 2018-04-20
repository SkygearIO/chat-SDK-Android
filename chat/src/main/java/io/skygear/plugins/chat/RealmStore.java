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

import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static io.skygear.plugins.chat.MessageOperationCacheObject.KEY_OPERATION_ID;
import io.skygear.skygear.Error;

class RealmStore {
    static String TAG = "RealmStore";

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
                .modules(new SkygearChatModule())
                .schemaVersion(2)
                .migration(new SequenceMigration())
                .schemaVersion(3)
                .migration(new ParticipantMigration());

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
                Log.e(TAG, "Faulty Message Found: " + cacheObject.recordID, e);
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
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
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
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
        }
    }

    private void getMessageOperations(final RealmResults<MessageOperationCacheObject> results,
                                      final int limit,
                                      final ResultCallback<MessageOperation[]> callback) {
        OrderedRealmCollectionSnapshot<MessageOperationCacheObject> snapshot = results.createSnapshot();

        int resolvedLimit = limit;
        int size = snapshot.size();
        if (limit == -1 || limit > size) {
            resolvedLimit = size;
        }

        List<MessageOperation> operations = new ArrayList<>(resolvedLimit);
        List<MessageOperationCacheObject> faultyCacheObjects = new ArrayList<>();
        for (int i = 0; i < resolvedLimit; i++) {
            MessageOperationCacheObject cacheObject = snapshot.get(i);
            if (cacheObject == null) {
                throw new RuntimeException("Unexpected null object when getting message query result");
            }

            MessageOperation operation;
            try {
                operation = cacheObject.toMessageOperation();
                operations.add(operation);
            } catch (Exception e) {
                Log.e(TAG, "Faulty Operation Found: " + cacheObject.operationID, e);
                faultyCacheObjects.add(cacheObject);
            }
        }

        if (faultyCacheObjects.size() > 0) {
            Realm realm = getRealm();
            realm.beginTransaction();

            // clear up faulty cache objects
            for (MessageOperationCacheObject cacheObject : faultyCacheObjects) {
                cacheObject.deleteFromRealm();
            }

            realm.commitTransaction();
        }

        MessageOperation[] operationArray = new MessageOperation[operations.size()];
        callback.onResultGet(operations.toArray(operationArray));
    }

    void getMessageOperations(@Nonnull final QueryBuilder<MessageOperationCacheObject> queryBuilder,
                              final int limit,
                              @Nonnull final String order,
                              @Nonnull final ResultCallback<MessageOperation[]> callback) {
        RealmQuery<MessageOperationCacheObject> query = getRealm().where(MessageOperationCacheObject.class);
        query = queryBuilder.buildQueryFrom(query);
        if (this.async) {
            final RealmResults<MessageOperationCacheObject> results = query.findAllSortedAsync(order, Sort.DESCENDING);
            results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<MessageOperationCacheObject>>() {
                @Override
                public void onChange(RealmResults<MessageOperationCacheObject> operationCacheObjects, @Nullable OrderedCollectionChangeSet changeSet) {
                    results.removeAllChangeListeners();
                    RealmStore.this.getMessageOperations(operationCacheObjects, limit, callback);
                }
            });
        } else {
            final RealmResults<MessageOperationCacheObject> results = query.findAllSorted(order, Sort.DESCENDING);
            this.getMessageOperations(results, limit, callback);
        }
    }

    void getMessageOperationWithID(@Nonnull final String operationID,
                                   @Nonnull final ResultCallback<MessageOperation> callback) {
        final ResultCallback<MessageOperation[]> wrappedCallback = new ResultCallback<MessageOperation[]>() {
            @Override
            public void onResultGet(MessageOperation[] result) {
                if (result.length == 0) {
                    callback.onResultGet(null);
                } else {
                    callback.onResultGet(result[0]);
                }
            }
        };

        if (this.async) {
            final RealmResults<MessageOperationCacheObject> results = getRealm().where(MessageOperationCacheObject.class).equalTo(KEY_OPERATION_ID, operationID).findAllAsync();
            results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<MessageOperationCacheObject>>() {
                @Override
                public void onChange(RealmResults<MessageOperationCacheObject> operationCacheObjects, @Nullable OrderedCollectionChangeSet changeSet) {
                    results.removeAllChangeListeners();
                    RealmStore.this.getMessageOperations(operationCacheObjects, 1, wrappedCallback);
                }
            });
        } else {
            final RealmResults<MessageOperationCacheObject> results = getRealm().where(MessageOperationCacheObject.class).equalTo(KEY_OPERATION_ID, operationID).findAll();
            this.getMessageOperations(results, 1, wrappedCallback);
        }
    }

    private void setMessageOperations(final Realm realm, final MessageOperation[] operations) {
        List<MessageOperationCacheObject> cacheObjects = new ArrayList<>(operations.length);
        for (MessageOperation operation : operations) {
            MessageOperationCacheObject cacheObject = new MessageOperationCacheObject(operation);
            cacheObjects.add(cacheObject);
        }

        realm.insertOrUpdate(cacheObjects);
    }

    void setMessageOperations(final MessageOperation[] operations) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmStore.this.setMessageOperations(realm, operations);
            }
        };

        if (this.async) {
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
        }
    }

    private void deleteMessageOperations(final Realm realm, final MessageOperation[] operations) {
        String[] operationIDs = new String[operations.length];
        for (int i = 0; i < operations.length; i++) {
            operationIDs[i] = operations[i].getId();
        }

        RealmResults<MessageOperationCacheObject> cacheObjects =
                realm.where(MessageOperationCacheObject.class).in(KEY_OPERATION_ID, operationIDs).findAll();
        cacheObjects.deleteAllFromRealm();
    }

    void deleteMessageOperations(final MessageOperation[] operations) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmStore.this.deleteMessageOperations(realm, operations);
            }
        };

        if (this.async) {
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
        }
    }

    private void markMessageOperationsAsFailed(final Realm realm,
                                               @Nonnull final QueryBuilder<MessageOperationCacheObject> queryBuilder,
                                               @Nullable final Error error) {
        RealmQuery<MessageOperationCacheObject> query = realm.where(MessageOperationCacheObject.class);
        query = queryBuilder.buildQueryFrom(query);

        final RealmResults<MessageOperationCacheObject> results = query.findAll();
        for (MessageOperationCacheObject cacheObject: results) {
            cacheObject.status = MessageOperation.Status.FAILED.toString();
            cacheObject.errorData = error != null ? ErrorSerializer.serialize(error).toString() : null;
        }
        realm.copyToRealmOrUpdate(results);
    }

    void markMessageOperationsAsFailed(@Nonnull final QueryBuilder<MessageOperationCacheObject> queryBuilder,
                                       @Nullable final Error error) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            RealmStore.this.markMessageOperationsAsFailed(realm, queryBuilder, error);
            }
        };

        // This operation only supports sync query because the operation is done on the main
        // thread before the looper is instantiated.
        getRealm().executeTransaction(transaction);
    }

    void deleteAll() {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        };

        if (this.async) {
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
        }
    }

    void setParticipants(final Collection<Participant> participants) {
        Realm.Transaction transaction = new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmStore.this.setParticipants(realm, participants);
            }
        };

        if (this.async) {
            getRealm().executeTransactionAsync(transaction);
        } else {
            getRealm().executeTransaction(transaction);
        }
    }

    private void setParticipants(final Realm realm, final Collection<Participant> participants) {
        List<ParticipantCacheObject> cacheObjects = new ArrayList<>(participants.size());
        for (Participant participant : participants) {
            ParticipantCacheObject cacheObject = new ParticipantCacheObject(participant);
            cacheObjects.add(cacheObject);
        }
        realm.insertOrUpdate(cacheObjects);
    }

    void getParticipantsWithIds(@Nonnull final Collection<String> participantIds,
                                @Nonnull final GetCallback<Map<String, Participant>> callback) {
        if (callback != null) {
            RealmQuery<ParticipantCacheObject> query = null;
            if (participantIds == null || participantIds.isEmpty()) {
                query = getRealm().where(ParticipantCacheObject.class);
            } else {
                query = getRealm().where(ParticipantCacheObject.class).in(ParticipantCacheObject.KEY_RECORD_ID,
                        participantIds.toArray(new String[participantIds.size()]));
            }

            if (this.async) {
                final RealmResults<ParticipantCacheObject> results = query.findAllAsync();
                results.addChangeListener(
                        new OrderedRealmCollectionChangeListener<RealmResults<ParticipantCacheObject>>() {
                            @Override
                            public void onChange(RealmResults<ParticipantCacheObject> participantCacheObjects, @Nullable OrderedCollectionChangeSet changeSet) {
                                results.removeAllChangeListeners();
                                RealmStore.this.getParticipants(participantCacheObjects, callback);
                            }
                        }
                );
            } else {
                final RealmResults<ParticipantCacheObject> results = query.findAll();
                RealmStore.this.getParticipants(results, callback);
            }
        }
    }

    void getParticipants(RealmResults<ParticipantCacheObject> results, GetCallback<Map<String, Participant>> callback) {
        HashMap<String, Participant> participantsMap = new HashMap<>();
        for (ParticipantCacheObject object: results) {
            try {
                participantsMap.put(object.recordID, object.toParticipant());
            } catch (JSONException e) {
                Log.e(TAG, "Failed to serialize Chat User, reason: " + e.getMessage());
            }
        }
        callback.onSuccess(participantsMap);
    }

    @RealmModule(library = true, classes = {MessageCacheObject.class, MessageOperationCacheObject.class, ParticipantCacheObject.class})
    private static class SkygearChatModule {

    }
}
