package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.utils.ShapeImageView
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.Message


class OutgoingTextMessageView(itemView: View) : MessageHolders.OutcomingTextMessageViewHolder<Message>(itemView) {

    protected var username: TextView? = null
    protected var avatarImageView: ShapeImageView? = null

    init {
        username = itemView.findViewById<TextView>(R.id.usernameText)
        avatarImageView = itemView.findViewById<ShapeImageView>(R.id.messageUserAvatar)
    }

    override fun onBind(message: Message) {
        super.onBind(message)

        username?.let {
            it.text = message.author?.name ?: ""
            it.visibility = if (it.text?.isEmpty() ?: true) View.GONE else View.VISIBLE
        }

        avatarImageView?.visibility = if (message.style.showReceiver) View.VISIBLE else View.GONE
    }
}
