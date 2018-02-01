package io.skygear.plugins.chat.ui.model

import io.skygear.chatkit.commons.models.IUser

import io.skygear.plugins.chat.ChatUser
import io.skygear.plugins.chat.ui.AvatarType
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.skygear.Asset
import io.skygear.skygear.Record

class User : IUser {
    companion object {
        var DefaultDisplayName = ""
        var DefaultUsernameField = "username"
        var DefaultAvatarField = "Unknown"
    }

    val chatUser: ChatUser?
    val chatUserId: String
    val avatarField: String?
    val displayNameField: String?
    var avatarType: AvatarType
    var avatarBackgroundColor: Int
    var avatarTextColor: Int

    constructor(record: Record,
                displayNameField: String?,
                avatarField: String? ,
                avatarType: AvatarType,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.chatUser = ChatUser.fromJson(record.toJson())
        this.chatUserId = record.id
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarTextColor = avatarInitialTextColor
    }

    constructor(recordID: String,
                displayNameField: String?,
                avatarField: String? ,
                avatarType: AvatarType,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.chatUser = null
        this.chatUserId = recordID
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarTextColor = avatarInitialTextColor
    }

    override fun getId() = this.chatUserId

    override fun getName(): String {
        if (this.chatUser == null) {
            return DefaultDisplayName
        }

        val userName = this.chatUser.record.get(this.displayNameField) as String?
        return userName ?: User.DefaultDisplayName
    }

    override fun getAvatar(): String {
        if (this.chatUser == null) {
            return AvatarBuilder.avatarUriForName("", this.avatarBackgroundColor, this.avatarTextColor)
        }

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

        val userName = this.chatUser.record.get(this.displayNameField) as String? ?: ""
        return AvatarBuilder.avatarUriForName(userName, this.avatarBackgroundColor, this.avatarTextColor)
    }


}
