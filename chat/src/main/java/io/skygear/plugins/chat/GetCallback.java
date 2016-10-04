package io.skygear.plugins.chat;


import android.support.annotation.Nullable;

import java.util.List;

public interface GetCallback<T> {
    void done(@Nullable List<T> objects, @Nullable String failReason);
}
