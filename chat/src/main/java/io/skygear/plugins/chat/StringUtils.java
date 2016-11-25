package io.skygear.plugins.chat;

import android.support.annotation.Nullable;

final class StringUtils {
    private StringUtils() {

    }
    static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
