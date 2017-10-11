package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IDialog
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.skygear.Record
import org.json.JSONException
import java.util.*
import io.skygear.plugins.chat.Conversation as ChatConversation

class Conversation: IDialog<Message> {
    companion object {
        /**
         * The field name for thumbnail of the conversation
         */
        var ThumbnailField = "thumbnail"
    }

    val chatConversation: ChatConversation
    var userList: List<User> = LinkedList()

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

    override fun getDialogName(): String? = this.chatConversation.title

    override fun getId(): String = this.chatConversation.id

    override fun getLastMessage(): Message? {
        this.chatConversation.lastMessage?.let { lastChatMessage ->
            return Message(lastChatMessage)
        }

        return null
    }

    override fun setLastMessage(message: Message?) {
        // TODO: set last message
    }

    override fun getUsers(): List<User> = this.userList

    override fun getDialogPhoto(): String? {
        val thumbnailUrl
                = this.chatConversation.record.get(Conversation.ThumbnailField) as String?
        if (thumbnailUrl != null) {
            return thumbnailUrl
        }

        val dialogName = this.dialogName ?: ""
        return AvatarBuilder.defaultBuilder().avatarUriForName(dialogName)
    }

    override fun getUnreadCount(): Int = this.chatConversation.unreadCount

}
