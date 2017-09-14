package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IUser

import org.json.JSONException

import io.skygear.plugins.chat.ChatUser
import io.skygear.skygear.Record

class User : IUser {
    companion object {
        /**
         * The field name for display name of the user
         */
        var DisplayNameField = "username"

        /**
         * The field name for avatar of the user
         */
        var AvatarField = "avatar"
    }

    private val chatUser: ChatUser

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

    override fun getId(): String {
        return this.chatUser.id
    }

    override fun getName(): String {
        return this.chatUser.record.get(User.DisplayNameField) as String
    }

    override fun getAvatar(): String {
        return this.chatUser.record.get(User.AvatarField) as String
    }


}
