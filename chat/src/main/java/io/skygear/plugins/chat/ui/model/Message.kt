package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IMessage
import io.skygear.skygear.Record
import org.json.JSONException
import java.util.*
import io.skygear.plugins.chat.Message as ChatMessage

class Message: IMessage {

    private val chatMessage: ChatMessage

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

    override fun getId(): String {
        return this.chatMessage.id
    }

    override fun getCreatedAt(): Date {
        return this.chatMessage.record.createdAt
    }

    override fun getUser(): User {
        TODO("get user from user cache")
    }

    override fun getText(): String? {
        return this.chatMessage.body
    }
}