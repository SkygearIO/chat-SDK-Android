package io.skygear.plugins.chat.chatUser;


import android.support.annotation.Nullable;

public class ChatUser {
    private final String id;
    private final String name;

    ChatUser(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }
}
