package io.skygear.plugins.chat.ui.model

import android.net.Uri
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.MessageContentType
import io.skygear.skygear.Record
import org.json.JSONException
import java.util.*
import io.skygear.plugins.chat.Message as ChatMessage

class Message: IMessage,
        MessageContentType.Image{

    val chatMessage: ChatMessage
    val chatMessageImageUrl: String?
    var author: User? = null


    constructor(record: Record) {
        try {
            this.chatMessage = ChatMessage.fromJson(record.toJson())
            this.chatMessageImageUrl = this.imageUrlFromChatMessage(this.chatMessage)
        } catch (e: JSONException) {
            throw IllegalArgumentException("Cannot serialize the skygear record")
        }
    }

    constructor(m: ChatMessage) {
        this.chatMessage = m
        this.chatMessageImageUrl = this.imageUrlFromChatMessage(m)
    }

    override fun getId(): String = this.chatMessage.id

    override fun getCreatedAt(): Date = this.chatMessage.record.createdAt

    override fun getUser(): User? = this.author

    override fun getText(): String? = this.chatMessage.body

    override fun getImageUrl(): String? = this.chatMessageImageUrl

    fun imageUrlFromChatMessage(chatMessage: ChatMessage): String? {
        var url = null as String?
        if (chatMessage.asset?.mimeType != null
                && chatMessage.asset?.mimeType!!.startsWith("image")) {
            url = chatMessage.asset?.url
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
        }
        return url
    }
}
