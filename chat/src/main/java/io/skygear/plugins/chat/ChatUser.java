package io.skygear.plugins.chat;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The Skygear Chat Plugin - ChatUser.
 */
public class ChatUser {
    private final String id;
    private final String name;

    ChatUser(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the chat user id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Gets the chat user name.
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
    }
}
