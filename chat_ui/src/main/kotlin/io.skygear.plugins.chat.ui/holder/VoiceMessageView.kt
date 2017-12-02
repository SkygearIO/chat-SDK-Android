package io.skygear.plugins.chat.ui.holder

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.VoiceMessage


class VoiceMessageView(itemView: View){

    var actionButton: ImageView? = null
    var durationTextView: TextView? = null
    var timeTextView: TextView? = null

    init {
        this.actionButton = itemView.findViewById<ImageView>(R.id.action_button)
        this.durationTextView = itemView.findViewById<TextView>(R.id.duration)
        this.timeTextView = itemView.findViewById<TextView>(R.id.messageTime)
    }

    fun onBind(message: VoiceMessage) {
        message.let { msg ->
            val durationInSecond = msg.duration / 1000
            this@VoiceMessageView.durationTextView?.text =
                    String.format("%02d:%02d", durationInSecond / 60, durationInSecond % 60)

            this@VoiceMessageView.timeTextView?.setTextColor(ContextCompat.getColor(actionButton?.context, io.skygear.chatkit.R.color.dark_gray))
            val actionButtonIcon = when (msg.state) {
                VoiceMessage.State.PLAYING -> R.drawable.ic_pause_white
                else -> R.drawable.ic_play_white
            }
            this@VoiceMessageView.actionButton?.setImageResource(actionButtonIcon)
        }
    }
}
