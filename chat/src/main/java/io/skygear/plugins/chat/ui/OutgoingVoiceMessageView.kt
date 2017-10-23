package io.skygear.plugins.chat.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.VoiceMessage

class OutgoingVoiceMessageView(view: View):
        MessageHolders.OutcomingTextMessageViewHolder<VoiceMessage>(view)
{
    var actionButton: ImageView? = null
    var durationTextView: TextView? = null
    var timeTextView: TextView? = null

    init {
        this.actionButton = view.findViewById<ImageView>(R.id.action_button)
        this.durationTextView = view.findViewById<TextView>(R.id.duration)
        this.timeTextView = view.findViewById<TextView>(R.id.time)
    }

    override fun onBind(message: VoiceMessage?) {
        super.onBind(message)
        message?.let { msg ->
            val durationInSecond = msg.duration / 1000
            this@OutgoingVoiceMessageView.durationTextView?.text =
                    String.format("%02d:%02d", durationInSecond / 60, durationInSecond % 60)

            this@OutgoingVoiceMessageView.timeTextView?.text =
                    DateFormatter.format(msg.createdAt, DateFormatter.Template.TIME)

            val actionButtonIcon = when(msg.state) {
                VoiceMessage.State.PLAYING -> R.drawable.ic_pause_white
                else -> R.drawable.ic_play_white
            }
            this@OutgoingVoiceMessageView.actionButton?.setImageResource(actionButtonIcon)
        }
    }
}
