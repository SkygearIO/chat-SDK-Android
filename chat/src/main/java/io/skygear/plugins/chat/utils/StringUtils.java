package io.skygear.plugins.chat.utils;


public final class StringUtils {
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
