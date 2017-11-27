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

    private Realm getRealm() {
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
                throw new RuntimeException("query limit should not exceed result size");
            }

            Message message = cacheObject.toMessage();
            if (message != null) {
                messages.add(message);
            } else {
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
        MessageCacheObject cacheObject = getRealm().where(MessageCacheObject.class).equalTo("recordID", messageID).findFirst();
        if (cacheObject == null) {
            return null;
        }

        return cacheObject.toMessage();
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
                realm.where(MessageCacheObject.class).in("recordID", messageIDs).findAll();

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
