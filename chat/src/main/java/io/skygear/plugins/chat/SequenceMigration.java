package io.skygear.plugins.chat;


import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class SequenceMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            final RealmObjectSchema messageSchema = schema.get("MessageCacheObject");
            messageSchema.addField("seq", Integer.class);
        }
    }

    /*
     *  From: https://stackoverflow.com/questions/36907001/open-realm-with-new-realmconfiguration
     */

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof SequenceMigration);
    }
}
