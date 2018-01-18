package io.skygear.plugins.chat.ui.holder

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import io.skygear.chatkit.R
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.MessageBubbleStyle

open class BaseBubbleMessageView {
    val bubble: ViewGroup?
    val background: Drawable

    constructor(itemView: View, drawable: Int) {
        this.bubble = itemView.findViewById<ViewGroup>(R.id.bubble)
        this.background = ContextCompat.getDrawable(itemView.context, drawable).constantState.newDrawable()
    }

    fun onBind(message: Message) {
        background.mutate()
        DrawableCompat.setTint(background, backgroundColor(message.style.bubbleStyle))
        bubble?.background  = background
    }

    open fun backgroundColor(style: MessageBubbleStyle): Int {
        return -1
    }
}