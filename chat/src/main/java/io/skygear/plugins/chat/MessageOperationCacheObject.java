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
import io.skygear.skygear.Error;

/**
 * The Realm model of Skygear message.
 */
public class MessageOperationCacheObject extends RealmObject {

    static String KEY_OPERATION_ID = "operationID";
    static String KEY_MESSAGE_ID = "messageID";
    static String KEY_CONVERSATION_ID = "conversationID";
    static String KEY_TYPE = "type";
    static String KEY_STATUS = "status";
    static String KEY_SEND_DATE = "sendDate";
    static String KEY_JSON_DATA = "jsonData";
    static String KEY_ERROR_DATA = "errorData";

    @PrimaryKey
    String operationID;

    String messageID;

    String conversationID;

    String type;

    String status;

    Date sendDate;

    String jsonData;

    String errorData;


    public MessageOperationCacheObject() {
        // for realm
    }

    public MessageOperationCacheObject(MessageOperation operation) {
        this.operationID = operation.getId();
        this.messageID = operation.getMessage().getId();
        this.conversationID = operation.getConversationId();
        this.type = operation.getType().getName();
        this.status = operation.getStatus().getName();
        this.sendDate = operation.getSendDate();
        this.jsonData = operation.getMessage().toJson().toString();
        Error operationError = operation.getError();
        if (operationError != null) {
            this.errorData = ErrorSerializer.serialize(operationError).toString();
        }
    }

    MessageOperation toMessageOperation() throws Exception {
        Message message = Message.fromJson(new JSONObject(this.jsonData));

        Error error = null;
        if (this.errorData != null) {
            error = ErrorSerializer.deserialize(new JSONObject(this.errorData));
        }
        return new MessageOperation(this.operationID,
                                    message,
                                    this.conversationID,
                                    MessageOperation.Type.fromName(this.type),
                                    MessageOperation.Status.fromName(this.status),
                                    this.sendDate,
                                    error);
    }
}
