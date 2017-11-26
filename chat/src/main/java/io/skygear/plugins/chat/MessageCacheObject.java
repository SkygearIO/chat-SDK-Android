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
