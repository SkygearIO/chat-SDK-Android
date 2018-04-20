package io.skygear.plugins.chat;

import org.json.JSONException;
import org.json.JSONObject;

import io.skygear.skygear.Asset;
import android.util.Base64;

/**
 * AssetSerializer which serializes and de-serializes data field. For MessageOperationCacheObject only.
 */

public class MessageAssetSerializer {

    /**
     * Serialize an asset
     *
     * @param asset the asset
     * @return the json object
     */
    public static JSONObject serialize(Asset asset) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$type", "asset");
            jsonObject.put("$name", asset.getName());
            jsonObject.put("$content_type", asset.getMimeType());

            if (asset.getUrl() != null) {
                jsonObject.put("$url", asset.getUrl());
            }

            if (asset.getData() != null) {
                jsonObject.put("data", Base64.encodeToString(asset.getData(), Base64.DEFAULT));
            }

            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Deserialize an asset from json object.
     *
     * @param assetJSONObject the asset json object
     * @return the asset
     * @throws JSONException the json exception
     */
    public static Asset deserialize(JSONObject assetJSONObject) throws JSONException {
        String typeValue = assetJSONObject.getString("$type");
        if (typeValue.equals("asset")) {
            String assetName = assetJSONObject.getString("$name");
            String assetMimeType = assetJSONObject.getString("$content_type");
            if (assetJSONObject.has("data")) {
                byte[] data = Base64.decode(assetJSONObject.getString("data"), Base64.DEFAULT);
                return new Asset(assetName, assetMimeType, data);
            }
            String assetUrl = assetJSONObject.optString("$url", null);
            return new Asset(assetName, assetUrl, assetMimeType);
        }

        throw new JSONException("Invalid $type value: " + typeValue);
    }
}
