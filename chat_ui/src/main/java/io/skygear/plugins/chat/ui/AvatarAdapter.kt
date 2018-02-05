package io.skygear.plugins.chat.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.model.User
import java.io.Serializable

open class AvatarAdapter : Serializable {
    open fun createAvatarView(inflater: LayoutInflater, viewGroup: ViewGroup): View? { return null }
    open fun bind(view: View, conversation: Conversation?, message: Message, user: User?) {}
}
