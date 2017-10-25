package io.skygear.plugins.chat.error;



import android.support.annotation.Nullable;

import io.skygear.skygear.Error;

public class AuthenticationError extends Error {
    public AuthenticationError(@Nullable String message) {
        super(Code.INVALID_ARGUMENT.getValue(), message);
    }
}
