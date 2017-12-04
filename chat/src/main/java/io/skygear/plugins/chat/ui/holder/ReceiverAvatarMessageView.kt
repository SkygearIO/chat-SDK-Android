package io.skygear.plugins.chat.ui.holder



import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.stfalcon.chatkit.R
import io.skygear.plugins.chat.ui.model.Message
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.utils.DateFormatter;


class ReceiverAvatarMessageView(itemView: View){

    var userAvatar: LinearLayout? = null
    var time: TextView? = null

    init {
        time = itemView.findViewById<TextView>(R.id.messageTime) as TextView
        userAvatar = itemView.findViewById<LinearLayout>(io.skygear.plugins.chat.R.id.userAvatar) as LinearLayout
    }

    fun onBind(message: Message) {
        userAvatar?.visibility = if (message.style.hideIncoming) View.GONE else View.VISIBLE

        if (userAvatar?.visibility == View.VISIBLE && userAvatar != null) {
            val isAvatarExists = ! ( message.user?.avatar?.isEmpty() ?: false)

            userAvatar!!.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
        }

        time?.text = message.getStatus() + " " + DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME)

    }
}
