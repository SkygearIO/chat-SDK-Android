package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.ImageMessage


/**
 * Created by carmenlau on 10/17/17.
 */
class IncomingImageMessageView(itemView: View) : MessageHolders.IncomingImageMessageViewHolder<ImageMessage>(itemView) {

    protected var username: TextView? = null

    init {
        username = itemView.findViewById<TextView>(R.id.usernameText)
    }

    override fun onBind(message: ImageMessage) {
        super.onBind(message)

        username?.let {
            it.text = message.author?.name ?: ""
            it.visibility = if (it.text?.isEmpty() ?: true) View.GONE else View.VISIBLE
        }
    }
}
