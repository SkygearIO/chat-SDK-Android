package io.skygear.plugins.chat;

import android.support.annotation.Nullable;

/**
 * The String Utils.
 */
final class StringUtils {
    private StringUtils() {

    }

    /**
     * Check whether a string is empty.
     *
     * @param cs the character wequence
     * @return the boolean
     */
    static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
