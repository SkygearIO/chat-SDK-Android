/*
 * Copyright 2018 Oursky Ltd.
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


import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class ChatUserMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion == 2) {
            RealmSchema schema = realm.getSchema();
            schema.create("ChatUserCacheObject")
                    .addField(ChatUserCacheObject.KEY_RECORD_ID, String.class)
                    .addField(ChatUserCacheObject.KEY_JSON_DATA, String.class)
                    .addPrimaryKey(ChatUserCacheObject.KEY_RECORD_ID);
        }
    }

    @Override
    public int hashCode() {
        return 39;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ChatUserMigration);
    }
}
