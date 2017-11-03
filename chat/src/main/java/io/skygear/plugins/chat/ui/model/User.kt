package io.skygear.plugins.chat.ui.model

import com.stfalcon.chatkit.commons.models.IUser

import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.ui.AvatarType
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.skygear.Asset
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
    var avatarType: AvatarType
    var avatarBackgroundColor: Int
    var avatarInitialTextColor: Int

    constructor(record: Record,
                displayNameField: String?,
                avatarField: String? ,
                avatarType: AvatarType,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.chatUser = ChatUser.fromJson(record.toJson())
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarInitialTextColor = avatarInitialTextColor
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
        if (this.avatarType == AvatarType.IMAGE) {

            var avatarUrl: String? = null
            val field = this.chatUser.record.get(this.avatarField)

            if (field is Asset) {
                avatarUrl = field.url
            }

            if (field is String) {
                avatarUrl = field
            }

            if (avatarUrl != null) {
                return avatarUrl
            }
        }

        return AvatarBuilder.avatarUriForName(this.name, this.avatarBackgroundColor, this.avatarInitialTextColor)
    }


}
