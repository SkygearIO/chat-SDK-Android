package io.skygear.plugins.chat;

import android.support.annotation.NonNull;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.skygear.plugins.chat.Message;

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
        // TODO: from Message to MessageCacheObject
    }

    @NonNull
    Message toMessage() {
        // TODO: from MessageCacheObject to Message
        return null;
    }
}
