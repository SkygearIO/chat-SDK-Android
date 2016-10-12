package io.skygear.plugins.chat.unread;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.skygear.Container;
import io.skygear.skygear.LambdaResponseHandler;

/**
 * Unread Container for Skygear Chat Plugin.
 */
public class UnreadContainer {
    private static UnreadContainer sharedInstance;
    private final Container container;

    /**
     * Gets the Unread container of Chat Plugin shared within the application.
     *
     * @param container - skygear context
     * @return a Unread container
     */
    public static UnreadContainer getInstance(@NonNull final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new UnreadContainer(container);
        }

        return sharedInstance;
    }

    private UnreadContainer(final Container container) {
        this.container = container;
    }

    /**
     * Gets the unread count.
     *
     * @param callback - GetCallback instance to handle Unread instance
     */
    public void get(@Nullable final GetCallback<Unread> callback) {
        container.callLambdaFunction("chat:total_unread", null, new LambdaResponseHandler() {
            @Override
            public void onLambdaSuccess(JSONObject result) {
                try {
                    int count = result.getInt("message");
                    if (callback != null) {
                        callback.onSucc(new Unread(count));
                    }
                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onFail(e.getMessage());
                    }
                }
            }

            @Override
            public void onLambdaFail(String reason) {
                if (callback != null) {
                    callback.onFail(reason);
                }
            }
        });
    }
}
