package io.skygear.plugins.chat;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Error;

public class MessageAssetCacheHelper {
    private static final String TAG = "AssetCache";

    static void saveMessageAsset(Context context, Message message) {
        Asset asset = message.getAsset();
        if (asset != null) {
            File file = new File(context.getCacheDir(), message.getId());
            try {
                FileOutputStream fos = new FileOutputStream(file);
                JSONObject object = MessageAssetSerializer.serialize(asset);
                fos.write(object.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "Cannot save asset.", e);
            }
        }
    }

    public static Asset getAsset(Context context, String messageId) {
        File file = new File(context.getCacheDir(), messageId);
        Asset asset = null;
        try {
            if (file.exists()) {
                FileInputStream ios = new FileInputStream(file);
                int size = ios.available();
                byte[] buffer = new byte[size];
                ios.read(buffer);
                String jsonString = new String(buffer, "UTF-8");
                ios.close();
                JSONObject object = new JSONObject(jsonString);
                asset = MessageAssetSerializer.deserialize(object);
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot delete asset.", e);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot delete asset.", e);
        }
        return asset;
    }

    static void deleteMessageAsset(Context context, String messageId) {
        File file = new File(context.getCacheDir(), messageId);
        file.delete();
    }
}
