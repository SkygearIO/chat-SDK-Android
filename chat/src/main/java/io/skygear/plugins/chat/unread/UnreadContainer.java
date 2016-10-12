package io.skygear.plugins.chat.unread;


import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.plugins.chat.callbacks.GetCallback;
import io.skygear.skygear.Container;
import io.skygear.skygear.LambdaResponseHandler;

public class UnreadContainer {
    private static UnreadContainer sharedInstance;
    private final Container container;

    public static UnreadContainer getInstance(final Container container) {
        if (sharedInstance == null) {
            sharedInstance = new UnreadContainer(container);
        }

        return sharedInstance;
    }

    private UnreadContainer(final Container container) {
        this.container = container;
    }

    public void get(final GetCallback<Unread> callback) {
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
