package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IMessage
import io.skygear.skygear.Record
import org.json.JSONException
import java.util.*
import io.skygear.plugins.chat.Message as ChatMessage

open class Message: IMessage {

    val chatMessage: ChatMessage
    var author: User? = null


    constructor(record: Record) {
        try {
            this.chatMessage = ChatMessage.fromJson(record.toJson())
        } catch (e: JSONException) {
            throw IllegalArgumentException("Cannot serialize the skygear record")
        }
    }

    constructor(m: ChatMessage) {
        this.chatMessage = m
    }

    override fun getId(): String = this.chatMessage.id

    override fun getCreatedAt(): Date = this.chatMessage.record.createdAt ?: Date()

    override fun getUser(): User? = this.author

    override fun getText(): String? = this.chatMessage.body

    fun getStatus(): String {
        this.chatMessage.status?.getName()?.let {
            return it.replace("_", " ", true).capitalize()
        }
        return "Delivering"
    }
}
