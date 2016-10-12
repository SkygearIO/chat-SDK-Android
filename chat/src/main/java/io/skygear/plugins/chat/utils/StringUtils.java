package io.skygear.plugins.chat.utils;

import android.support.annotation.Nullable;

/**
 * The Skygear Chat Plugin - Utils for String class.
 */
public final class StringUtils {
    /**
     * Check if input String instance is empty.
     *
     * @param cs - input String instance
     * @return if the input String is empty
     */
    public static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
