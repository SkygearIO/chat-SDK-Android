package io.skygear.plugins.chat.unread;


public class Unread {
    private final int count;

    Unread(final int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
