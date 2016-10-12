package io.skygear.plugins.chat;

/**
 * The Skygear Chat Plugin - Unread.
 */
public class Unread {
    private final int count;

    /**
     * Instantiates a new Unread.
     *
     * @param count the unread count
     */
    Unread(final int count) {
        this.count = count;
    }

    /**
     * Gets the unread count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }
}
