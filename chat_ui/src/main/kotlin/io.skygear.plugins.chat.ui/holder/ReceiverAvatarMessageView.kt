package io.skygear.plugins.chat.ui.holder



import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.skygear.plugins.chat.ui.R
import io.skygear.plugins.chat.ui.model.Message
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.utils.DateFormatter


class ReceiverAvatarMessageView(itemView: View){

    var userAvatar: LinearLayout? = null
    var time: TextView? = null

    init {
        time = itemView.findViewById<TextView>(R.id.messageTime) as TextView
        userAvatar = itemView.findViewById<LinearLayout>(R.id.userAvatar) as LinearLayout
    }

    fun onBind(message: Message) {
        userAvatar?.visibility = if (message.style.showReceiver) View.VISIBLE else View.GONE

        if (userAvatar?.visibility == View.VISIBLE && userAvatar != null) {
            val isAvatarExists = ! ( message.user?.avatar?.isEmpty() ?: false)

            userAvatar!!.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
        }

        time?.text = message.getStatus() + " " + DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME)

    }
}
