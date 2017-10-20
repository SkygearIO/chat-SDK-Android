package io.skygear.plugins.chat.ui.holder

import android.view.View
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.model.Message


/**
 * Created by carmenlau on 10/17/17.
 */
class IncomingTextMessageView(itemView: View) : MessageHolders.IncomingTextMessageViewHolder<Message>(itemView) {

    protected var username: TextView? = null

    init {
        username = itemView.findViewById(R.id.usernameText) as TextView
    }

    override fun onBind(message: Message) {
        super.onBind(message)

        username?.let {
            it.text = message.author?.name ?: ""
            it.visibility = if (it.text?.isEmpty() ?: true) View.GONE else View.VISIBLE
        }
    }
}
