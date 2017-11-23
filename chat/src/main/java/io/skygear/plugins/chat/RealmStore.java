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

    @RealmModule(library = true, allClasses = true)
    private static class SkygearChatModule {

    }
}
