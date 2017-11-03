package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IUser

import org.json.JSONException

import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.skygear.Record

class User : IUser {
    companion object {
        var DefaultDisplayName = "Unknown"
        var DefaultUsernameField = "username"
        var DefaultAvatarField = "Unknown"
    }

    val chatUser: ChatUser
    val avatarField: String?
    val displayNameField: String?

    constructor(record: Record, displayNameField: String? = DefaultUsernameField, avatarField: String? = DefaultAvatarField):
            this(ChatUser.fromJson(record.toJson()), displayNameField, avatarField)


    constructor(u: ChatUser, displayNameField: String? = DefaultUsernameField, avatarField: String? = DefaultAvatarField) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.chatUser = u
    }

    override fun getId() = this.chatUser.id

    override fun getName(): String {
        val userName = this.chatUser.record.get(this.displayNameField) as String?
        if (userName != null) {
            return userName
        }

        return User.DefaultDisplayName
    }

    override fun getAvatar(): String {
        val avatarUrl = this.chatUser.record.get(this.avatarField) as String?
        if (avatarUrl != null) {
            return avatarUrl
        }

        return AvatarBuilder.defaultBuilder().avatarUriForName(this.name)
    }


}
