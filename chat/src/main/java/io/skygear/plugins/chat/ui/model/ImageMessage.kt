package io.skygear.plugins.chat.ui.model

import android.net.Uri
import com.stfalcon.chatkit.commons.models.MessageContentType
import io.skygear.skygear.Record
import io.skygear.plugins.chat.Message as ChatMessage

/**
 * Created by carmenlau on 10/15/17.
 */

class ImageMessage: Message,
        MessageContentType.Image{

    val chatMessageImageUrl: String?

    constructor(record: Record) : super(record) {
        this.chatMessageImageUrl = this.imageUrlFromChatMessage(this.chatMessage)
    }

    constructor(m: ChatMessage) : super(m) {
        this.chatMessageImageUrl = this.imageUrlFromChatMessage(this.chatMessage)
    }

    override fun getImageUrl(): String? = this.chatMessageImageUrl

    fun imageUrlFromChatMessage(chatMessage: ChatMessage): String? {
        var url = chatMessage.asset?.url
        val meta = chatMessage.metadata
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

            url = builder.build().toString()
        }
        return url
    }
}
