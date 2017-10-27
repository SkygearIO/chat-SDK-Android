package io.skygear.plugins.chat.ui.holder

import io.skygear.plugins.chat.ui.model.VoiceMessage

class OutgoingVoiceMessageView(view: android.view.View):
        com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder<VoiceMessage>(view)
{
    var actionButton: android.widget.ImageView? = null
    var durationTextView: android.widget.TextView? = null
    var timeTextView: android.widget.TextView? = null

    init {
        this.actionButton = view.findViewById<android.widget.ImageView>(io.skygear.plugins.chat.R.id.action_button)
        this.durationTextView = view.findViewById<android.widget.TextView>(io.skygear.plugins.chat.R.id.duration)
        this.timeTextView = view.findViewById<android.widget.TextView>(io.skygear.plugins.chat.R.id.time)
    }

    override fun onBind(message: io.skygear.plugins.chat.ui.model.VoiceMessage?) {
        super.onBind(message)
        message?.let { msg ->
            val durationInSecond = msg.duration / 1000
            this@OutgoingVoiceMessageView.durationTextView?.text =
                    String.format("%02d:%02d", durationInSecond / 60, durationInSecond % 60)

            this@OutgoingVoiceMessageView.timeTextView?.text =
                    com.stfalcon.chatkit.utils.DateFormatter.format(msg.createdAt, com.stfalcon.chatkit.utils.DateFormatter.Template.TIME)

            val actionButtonIcon = when(msg.state) {
                io.skygear.plugins.chat.ui.model.VoiceMessage.State.PLAYING -> io.skygear.plugins.chat.R.drawable.ic_pause_white
                else -> io.skygear.plugins.chat.R.drawable.ic_play_white
            }
            this@OutgoingVoiceMessageView.actionButton?.setImageResource(actionButtonIcon)
        }
    }
}
