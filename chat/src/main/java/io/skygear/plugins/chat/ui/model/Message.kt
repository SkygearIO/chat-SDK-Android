package io.skygear.plugins.chat.ui.model

import io.skygear.chatkit.commons.models.IMessage
import java.util.*
import io.skygear.plugins.chat.Message as ChatMessage

open class Message: IMessage {

    val chatMessage: ChatMessage
    var author: User? = null
    var style: MessageStyle

    constructor(m: ChatMessage, style: MessageStyle) {
        this.chatMessage = m
        this.style = style
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
