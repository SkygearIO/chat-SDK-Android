package io.skygear.plugins.chat.ui.model

import android.net.Uri
import com.stfalcon.chatkit.commons.models.MessageContentType
import org.json.JSONObject
import io.skygear.plugins.chat.Message as ChatMessage


class ImageMessage: Message,
        MessageContentType.Image{

    val chatMessageImageUrl: String?


    constructor(m: ChatMessage, style: MessageStyle) : super(m, style) {
        this.chatMessageImageUrl = this.imageUrlFromChatMessage(
                this.chatMessage.asset?.url,
                this.chatMessage.metadata)
    }

    override fun getImageUrl(): String? = this.chatMessageImageUrl

    fun imageUrlFromChatMessage(imageUrl: String?, meta: JSONObject?): String? {
        var url = imageUrl
        if (url != null) {
            return imageUrl
        }
        meta?.let {
            val builder = Uri.parse("image://image")
                    .buildUpon()

            if (it.has("thumbnail")) {
                builder.appendQueryParameter("thumbnail", it.getString("thumbnail"))
            }

            if (it.has("width")) {
                builder.appendQueryParameter("width", it.getInt("width").toString())
            }

            if (it.has("height")) {
                builder.appendQueryParameter("height", it.getInt("height").toString())
            }

            url = builder.build().toString()
        }
        return url
    }
}
