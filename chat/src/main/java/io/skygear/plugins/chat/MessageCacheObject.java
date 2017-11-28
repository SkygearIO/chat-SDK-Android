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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.skygear.plugins.chat.Message;
import io.skygear.skygear.Record;

public class MessageCacheObject extends RealmObject {

    @PrimaryKey
    String recordID;

    String conversationID;

    Date creationDate;

    Date editionDate;

    Date sendDate;

    boolean deleted;

    boolean alreadySyncToServer;

    boolean fail;

    String jsonData;

    public MessageCacheObject() {
        // for realm
    }

    public MessageCacheObject(Message message) {
        this.recordID = message.getId();
        this.conversationID = message.getConversationId();
        this.creationDate = message.getCreatedTime();
        this.editionDate = (Date) message.record.get("edited_at");
        this.deleted = (Boolean) message.record.get("deleted");
        this.jsonData = message.toJson().toString();
    }

    Message toMessage() {
        try {
            JSONObject json = new JSONObject(this.jsonData);
            Record record = Record.fromJson(json);
            Message message = new Message(record);
            return message;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
