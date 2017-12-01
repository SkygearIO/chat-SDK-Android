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

    private final String name;
    private final boolean inMemory;

    RealmStore(String name, boolean inMemory) {
        this.name = name;
        this.inMemory = inMemory;
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

    Message[] getMessages(QueryBuilder<MessageCacheObject> queryBuilder,
                          int limit,
                          String order) {
        RealmQuery<MessageCacheObject> query = getRealm().where(MessageCacheObject.class);
        query = queryBuilder.buildQueryFrom(query);
        OrderedRealmCollectionSnapshot<MessageCacheObject> results = query.findAllSorted(order, Sort.DESCENDING).createSnapshot();
        int size = results.size();
        if (limit == -1 || limit > size) {
            limit = size;
        }

        List<Message> messages = new ArrayList<>(limit);
        List<MessageCacheObject> faultyCacheObjects = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            MessageCacheObject cacheObject = results.get(i);
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

        // clear up faulty cache objects
        for (MessageCacheObject cacheObject : faultyCacheObjects) {
            cacheObject.deleteFromRealm();
        }

        Message[] messageArray = new Message[messages.size()];
        return messages.toArray(messageArray);
    }

    Message getMessageWithID(String messageID) {
        MessageCacheObject cacheObject = getRealm().where(MessageCacheObject.class).equalTo(KEY_RECORD_ID, messageID).findFirst();
        if (cacheObject == null) {
            return null;
        }

        Message message;
        try {
            message = cacheObject.toMessage();
            return message;
        } catch (Exception e) {
            // clear up faulty cache objects
            cacheObject.deleteFromRealm();
            return null;
        }
    }

    void setMessages(Message[] messages) {
        Realm realm = getRealm();

        realm.beginTransaction();

        List<MessageCacheObject> cacheObjects = new ArrayList<>(messages.length);
        for (Message message : messages) {
            MessageCacheObject cacheObject = new MessageCacheObject(message);
            cacheObjects.add(cacheObject);
        }

        realm.insertOrUpdate(cacheObjects);

        realm.commitTransaction();
    }

    void deleteMessages(Message[] messages) {
        Realm realm = getRealm();

        String[] messageIDs = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            messageIDs[i] = messages[i].getId();
        }

        RealmResults<MessageCacheObject> cacheObjects =
                realm.where(MessageCacheObject.class).in(KEY_RECORD_ID, messageIDs).findAll();

        realm.beginTransaction();

        cacheObjects.deleteAllFromRealm();

        realm.commitTransaction();
    }

    void deleteAll() {
        Realm realm = getRealm();

        realm.beginTransaction();

        realm.deleteAll();

        realm.commitTransaction();
    }

    @RealmModule(library = true, allClasses = true)
    private static class SkygearChatModule {

    }
}
