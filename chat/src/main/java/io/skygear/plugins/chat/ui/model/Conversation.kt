package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IDialog
import io.skygear.skygear.Record
import org.json.JSONException
import io.skygear.plugins.chat.Conversation as ChatConversation

class Conversation: IDialog<Message> {
    val chatConversation: ChatConversation

    constructor(record: Record) {
        try {
            this.chatConversation = ChatConversation.fromJson(record.toJson())
        } catch (e: JSONException) {
            throw IllegalArgumentException("Cannot serialize the skygear record")
        }
    }

    constructor(c: ChatConversation) {
        this.chatConversation = c
    }

    override fun getDialogName(): String? {
        return this.chatConversation.title
    }

    override fun getId(): String {
        return this.chatConversation.id
    }

    override fun getLastMessage(): Message? {
        this.chatConversation.lastMessage?.let { lastChatMessage ->
            return Message(lastChatMessage)
        }

        return null
    }

    override fun setLastMessage(message: Message?) {
        TODO("set last message")
    }

    override fun getUsers(): MutableList<out User> {
        TODO("get users from cache list")
    }

    override fun getDialogPhoto(): String {
        TODO("get the thumbnail of the conversation")
    }

    override fun getUnreadCount(): Int {
        return this.chatConversation.unreadCount
    }

}
