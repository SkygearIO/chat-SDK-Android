package io.skygear.plugins.chat.ui.model

import io.skygear.chatkit.commons.models.IMessage
import io.skygear.plugins.chat.Message.Status
import java.util.*
import io.skygear.plugins.chat.Message as ChatMessage
import io.skygear.skygear.Error

open class Message: IMessage {
    val chatMessage: ChatMessage
    var author: User? = null
    var style: MessageStyle
    var statusText: MessageStatusText
    var error: Error? = null

    constructor(m: ChatMessage, style: MessageStyle) {
        this.chatMessage = m
        this.style = style
        this.statusText = style.statusText
    }

    override fun getId(): String = this.chatMessage.id

    override fun getCreatedAt(): Date = this.chatMessage.record.createdAt ?: this.chatMessage.sendDate ?: Date()

    override fun getUser(): User? = this.author

    override fun getText(): String? = this.chatMessage.body

    fun getStatus(): String {
        if (this.error != null) {
            return "Failed"
        }

        this.chatMessage.status?.let {
            when (it) {
                Status.DELIVERED -> return statusText.deliveredText
                Status.ALL_READ -> return statusText.allReadText
                Status.SOME_READ -> return statusText.someReadText
            }
        }
        return statusText.deliveringText
    }
}
