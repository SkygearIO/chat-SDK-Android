package io.skygear.plugins.chat.error;

import io.skygear.skygear.Error;

public class InvalidMessageError extends Error {
    public InvalidMessageError() {
        super(Code.INVALID_ARGUMENT.getValue(), "Invalid Message. Please provide either body, asset or metadata.");
    }
}
