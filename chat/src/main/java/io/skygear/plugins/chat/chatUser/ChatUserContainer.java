package io.skygear.plugins.chat.chatUser;


import java.util.ArrayList;
import java.util.List;

import io.skygear.plugins.chat.resps.GetResp;
import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;

public class ChatUserContainer {
    private static ChatUserContainer sharedInstance;

    private final Container container;

    public static ChatUserContainer getInstance(final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ChatUserContainer(container);
        }

        return sharedInstance;
    }

    private ChatUserContainer(final Container container) {
        this.container = container;
    }

    public void getAll(final GetCallback<List<ChatUser>> callback) {
        Query query = new Query("user");
        Database publicDB = container.getPublicDatabase();
        publicDB.query(query, new GetResp<List<ChatUser>>(callback) {
            @Override
            public List<ChatUser> onSuccess(Record[] records) {
                List<ChatUser> users = new ArrayList<>(records.length);

                for (Record record : records) {
                    String id = record.getId();
                    String name = (String) record.get("name");
                    if (id != null) {
                        users.add(new ChatUser(id, name));
                    }
                }

                return users;
            }
        });
    }
}
