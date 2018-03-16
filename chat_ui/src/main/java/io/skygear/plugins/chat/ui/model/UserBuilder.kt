package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.Participant
import io.skygear.plugins.chat.ui.AvatarType
import io.skygear.skygear.Record

class UserBuilder {
    val avatarField: String?
    val displayNameField: String?
    var avatarType: AvatarType
    var avatarBackgroundColor: Int
    var avatarTextColor: Int

    constructor(displayNameField: String? = User.DefaultUsernameField,
                avatarField: String? = User.DefaultAvatarField,
                avatarType: AvatarType = AvatarType.INITIAL,
                avatarBackgroundColor: Int,
                avatarInitialTextColor: Int) {
        this.avatarField = avatarField
        this.displayNameField = displayNameField
        this.avatarType = avatarType
        this.avatarBackgroundColor = avatarBackgroundColor
        this.avatarTextColor = avatarInitialTextColor
    }

    fun createUser(record: Record): User {
        return User(record, displayNameField, avatarField, avatarType, avatarBackgroundColor, avatarTextColor)
    }

    fun createUser(recordID: String): User {
        return User(recordID, displayNameField, avatarField, avatarType, avatarBackgroundColor, avatarTextColor)
    }

    fun createUser(participant: Participant): User {
        return User(participant, displayNameField, avatarField, avatarType, avatarBackgroundColor, avatarTextColor)
    }
}
