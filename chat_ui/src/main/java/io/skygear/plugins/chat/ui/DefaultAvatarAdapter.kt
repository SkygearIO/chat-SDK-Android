package io.skygear.plugins.chat.ui

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.User

open class DefaultAvatarAdapter() : AvatarAdapter() {
    override fun createAvatarView(inflater: LayoutInflater, viewGroup: ViewGroup): View {
        val imageView = inflater.inflate(R.layout.default_avatar_view, viewGroup, false)
        return imageView
    }

    override fun bind(view: View, conversation: Conversation?, message: Message, user: User?) {
        val avatarView = view as DefaultAvatarView
        avatarView.onBind(message)
    }
}