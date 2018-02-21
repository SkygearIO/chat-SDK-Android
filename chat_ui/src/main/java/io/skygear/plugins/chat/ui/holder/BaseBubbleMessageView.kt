package io.skygear.plugins.chat.ui.holder

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.skygear.chatkit.R
import io.skygear.plugins.chat.ui.R as UiKitResource
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.model.MessageBubbleStyle

open class BaseBubbleMessageView {
    val bubble: ViewGroup?
    val background: Drawable
    val isIncoming: Boolean
    val messageText: TextView?
    val durationText: TextView?

    constructor(itemView: View, drawable: Int, isIncoming: Boolean) {
        this.bubble = itemView.findViewById<ViewGroup>(R.id.bubble)
        this.messageText = itemView.findViewById<TextView>(R.id.messageText)
        this.durationText = itemView.findViewById<TextView>(UiKitResource.id.duration)
        this.background = ContextCompat.getDrawable(itemView.context, drawable).constantState.newDrawable()
        this.isIncoming = isIncoming
    }

    fun onBind(message: Message) {
        val color = backgroundColor(message.style.bubbleStyle)

        /*
          Workaround found on stackoverflow
          https://stackoverflow.com/questions/36731919/drawablecompat-settint-not-working-on-api-19
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(background, color);

        } else {
            background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        bubble?.background = background

        val textColor = if (isIncoming) message.style.bubbleStyle.textColorForIncomingMessages
                        else message.style.bubbleStyle.textColorForOutgoingMessages
        messageText?.setTextColor(textColor)
        durationText?.setTextColor(textColor)
    }

    open fun backgroundColor(style: MessageBubbleStyle): Int {
        return -1
    }
}