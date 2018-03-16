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

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.skygear.skygear.Record;

/**
 * The Realm model of Participant.
 */
public class ParticipantCacheObject extends RealmObject {

    static String KEY_RECORD_ID = "recordID";
    static String KEY_JSON_DATA = "jsonData";

    @PrimaryKey
    String recordID;
    String jsonData;

    public ParticipantCacheObject() {
        // for realm
    }

    public ParticipantCacheObject(Participant user) {
        this.recordID = user.getId();
        this.jsonData = user.getRecord().toJson().toString();
    }

    Participant toParticipant() throws JSONException {
        JSONObject json = new JSONObject(this.jsonData);
        Record record = Record.fromJson(json);
        Participant participant = new Participant(record);
        return participant;
    }
}
