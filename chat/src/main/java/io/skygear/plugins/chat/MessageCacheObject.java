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

/**
 * The Realm model of Skygear message.
 */
public class MessageCacheObject extends RealmObject {

    static String KEY_RECORD_ID = "recordID";
    static String KEY_CONVERSATION_ID = "conversationID";
    static String KEY_CREATION_DATE = "creationDate";
    static String KEY_EDITION_DATE = "editionDate";
    static String KEY_SEND_DATE = "sendDate";
    static String KEY_DELETED = "deleted";
    static String KEY_JSON_DATA = "jsonData";
    static String KEY_SEQ = "seq";

    @PrimaryKey
    String recordID;

    String conversationID;

    Date creationDate;

    Date editionDate;

    Date sendDate;

    Integer seq;

    boolean deleted;

    String jsonData;

    public MessageCacheObject() {
        // for realm
    }

    public MessageCacheObject(Message message) {
        this.recordID = message.getId();
        this.conversationID = message.getConversationId();
        this.creationDate = message.getCreatedTime();
        this.editionDate = (Date) message.record.get("edited_at");
        Boolean deleted = (Boolean) message.record.get("deleted");
        this.deleted = deleted == null ? false : deleted;
        this.jsonData = message.toJson().toString();
        this.sendDate = message.sendDate;
        this.seq = message.getSeq();
        // creationDate of the record originally represents the message creation date on server
        // this overloads the meaning of creationDate, to also represents local creation date.
        // Then creationDate can also be used to sort messages even not uploaded to server yet.
        //
        // This will not affect the Message created from cache object
        // because the deserialization of message record data is based on jsonData only.
        if (this.creationDate == null && message.sendDate != null) {
            this.creationDate = message.sendDate;
        }
    }

    Message toMessage() throws Exception {
        JSONObject json = new JSONObject(this.jsonData);
        Record record = Record.fromJson(json);
        Message message = new Message(record);
        message.sendDate = this.sendDate;
        return message;
    }
}
