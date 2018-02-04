package io.skygear.plugins.chat.ui.utils

import android.util.Base64
import io.skygear.plugins.chat.Message
import io.skygear.skygear.Asset
import org.json.JSONObject

class MessageBuilder {
    companion object {
        fun createImageMessage(imageData: ImageData): Message {

            val imageByteArray = bitmapToByteArray(imageData.image)
            val thumbByteArray = bitmapToByteArray(imageData.thumbnail)

            val meta = JSONObject()
            val encoded = Base64.encodeToString(thumbByteArray, Base64.DEFAULT)
            meta.put("thumbnail", encoded)
            meta.put("height", imageData.image.height)
            meta.put("width", imageData.image.width)

            val message = Message()
            message.asset = Asset("image.jpg", "image/jpeg", imageByteArray)
            message.metadata = meta

            return message
        }
    }
}