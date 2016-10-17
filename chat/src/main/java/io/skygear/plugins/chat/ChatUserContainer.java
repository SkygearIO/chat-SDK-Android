package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;

/**
 * ChatUser Container for Skygear Chat Plugin.
 */
public class ChatUserContainer {
    private static ChatUserContainer sharedInstance;

    private final Container container;

    /**
     * Gets the ChatUser container of Chat Plugin shared within the application.
     *
     * @param container - skygear context
     * @return a Conversation container
     */
    public static ChatUserContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new ChatUserContainer(container);
        }

        return sharedInstance;
    }

    private ChatUserContainer(final Container container) {
        this.container = container;
    }

    /**
     * Gets all chat users on the skygear.
     *
     * @param callback - GetCallback&lt;List&lt;ChatUser&gt;&gt; to handle result chat users
     */
    public void getAll(@Nullable final GetCallback<List<ChatUser>> callback) {
        Query query = new Query("user");
        Database publicDB = container.getPublicDatabase();
        publicDB.query(query, new GetResp<List<ChatUser>>(callback) {
            @Override
            public List<ChatUser> onSuccess(Record[] records) {
                List<ChatUser> users = new ArrayList<>(records.length);

                for (Record record : records) {
                    users.add(new ChatUser(record));
                }

                return users;
            }
        });
    }
}
