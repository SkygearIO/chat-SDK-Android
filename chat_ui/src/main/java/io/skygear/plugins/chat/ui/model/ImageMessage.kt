package io.skygear.plugins.chat.ui.model

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import io.skygear.chatkit.commons.models.MessageContentType
import org.json.JSONObject
import io.skygear.plugins.chat.Message as ChatMessage

/**
 * Created by carmenlau on 10/15/17.
 */

class ImageMessage : Message,
        MessageContentType.Image {

    val chatMessageImageUrl: String?

    constructor(m: ChatMessage, imageUri: Uri?, orientation: Int?, style: MessageStyle) : super(m, style) {
        this.chatMessageImageUrl = this.imageUrlFromChatMessage(
                this.chatMessage.asset?.url ?: imageUri?.toString(),
                this.chatMessage.metadata, orientation)
    }

    override fun getImageUrl(): String? = this.chatMessageImageUrl

    fun imageUrlFromChatMessage(imageUrl: String?, meta: JSONObject?, orientation: Int?): String? {
        var url = imageUrl
        if (url == null) {
            return null
        }
        meta?.let {
            val builder = Uri.parse(url)
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

            orientation?.let {
                builder.appendQueryParameter("orientation", orientation.toString())
            }

            url = builder.build().toString()
        }
        return url
    }
}
