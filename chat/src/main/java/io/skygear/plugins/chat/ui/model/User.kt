package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IUser

import org.json.JSONException

import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.skygear.Record

class User : IUser {
    companion object {
        /**
         * The field name for display name of the user
         */
        var DisplayNameField = "name"

        /**
         * The field name for avatar of the user
         */
        var AvatarField = "avatar"
    }

    val chatUser: ChatUser

    constructor(record: Record) {
        try {
            this.chatUser = ChatUser.fromJson(record.toJson())
        } catch (e: JSONException) {
            throw IllegalArgumentException("Cannot serialize the skygear record")
        }
    }

    constructor(u: ChatUser) {
        this.chatUser = u
    }

    override fun getId() = this.chatUser.id

    override fun getName()
        = this.chatUser.record.get(User.DisplayNameField) as String

    override fun getAvatar(): String {
        val avatarUrl = this.chatUser.record.get(User.AvatarField) as String?
        if (avatarUrl != null) {
            return avatarUrl
        }

        return AvatarBuilder.AvatarUriForName(this.name)
    }


}
