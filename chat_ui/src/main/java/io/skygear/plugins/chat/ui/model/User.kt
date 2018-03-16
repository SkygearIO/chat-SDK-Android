package io.skygear.plugins.chat.ui.model

import io.skygear.chatkit.commons.models.IUser

import io.skygear.plugins.chat.Participant
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

    val participant: Participant?
    val participantId: String
    val avatarField: String?
    val displayNameField: String?
    var avatarType: AvatarType
    var avatarBackgroundColor: Int
    var avatarTextColor: Int

    constructor(participant: Participant,
                displayNameField: String?,
                avatarField: String?,
                avatarType: AvatarType,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.participant = participant
        this.participantId = participant.id
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarTextColor = avatarInitialTextColor
    }

    constructor(record: Record,
                displayNameField: String?,
                avatarField: String? ,
                avatarType: AvatarType,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.participant = Participant.fromJson(record.toJson())
        this.participantId = record.id
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
        this.participant = null
        this.participantId = recordID
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarTextColor = avatarInitialTextColor
    }

    override fun getId() = this.participantId

    override fun getName(): String {
        if (this.participant == null) {
            return DefaultDisplayName
        }

        val userName = this.participant.record.get(this.displayNameField) as String?
        return userName ?: User.DefaultDisplayName
    }

    override fun getAvatar(): String {
        if (this.participant == null) {
            return AvatarBuilder.avatarUriForName("", this.avatarBackgroundColor, this.avatarTextColor)
        }

        if (this.avatarType == AvatarType.IMAGE) {
            var avatarUrl: String? = null
            val field = this.participant.record.get(this.avatarField)

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

        val userName = this.participant.record.get(this.displayNameField) as String? ?: ""
        return AvatarBuilder.avatarUriForName(userName, this.avatarBackgroundColor, this.avatarTextColor)
    }
}
