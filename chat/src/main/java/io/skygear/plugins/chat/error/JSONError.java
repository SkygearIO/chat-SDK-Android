package io.skygear.plugins.chat.error;

import io.skygear.skygear.Error;


public class JSONError extends Error {
    public JSONError() {
        super(Code.UNEXPECTED_ERROR.getValue(), "Cannot parse JSON.");
    }
}
