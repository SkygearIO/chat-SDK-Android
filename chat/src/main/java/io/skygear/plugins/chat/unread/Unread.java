package io.skygear.plugins.chat.unread;


public class Unread {
    private int count;

    Unread(final int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
