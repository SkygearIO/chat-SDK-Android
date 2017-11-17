package com.stfalcon.chatkit.messages
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.commons.ViewHolder
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.utils.DateFormatter
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.AvatarAdapter
import io.skygear.plugins.chat.R
import io.skygear.plugins.chat.ui.DefaultAvatarAdapter
import io.skygear.plugins.chat.ui.model.Message

class CustomMessageHolders(avatarAdapterFunc: () -> AvatarAdapter, conversationFunc: () -> Conversation?): MessageHolders() {
    var avatarAdapterFunc: () -> AvatarAdapter = { DefaultAvatarAdapter()}
    var conversationFunc: () -> Conversation? = {null}
    init {
        this.avatarAdapterFunc = avatarAdapterFunc
        this.conversationFunc = conversationFunc
    }

    override fun bind(holder: ViewHolder<*>?,
                      item: Any?,
                      isSelected: Boolean,
                      imageLoader: ImageLoader?,
                      onMessageClickListener: View.OnClickListener?,
                      onMessageLongClickListener: View.OnLongClickListener?,
                      dateHeadersFormatter: DateFormatter.Formatter?,
                      clickListenersArray: SparseArray<MessagesListAdapter.OnMessageViewClickListener<IMessage>>?) {
        super.bind(holder, item, isSelected, imageLoader, onMessageClickListener, onMessageLongClickListener, dateHeadersFormatter, clickListenersArray)
        val inflater = LayoutInflater.from(holder?.itemView?.context)
        val avatarContainerView = holder?.itemView?.findViewById<LinearLayout>(R.id.userAvatar)
        avatarContainerView?.let { containerView ->
            if (containerView.visibility == View.VISIBLE) {
                if (containerView.childCount == 0) {
                    containerView.addView(avatarAdapterFunc()?.createAvatarView(inflater, containerView))
                }
                val avatarView = containerView.getChildAt(0)
                avatarAdapterFunc()?.bind(avatarView, conversationFunc(),item as Message, item.user)
            }
        }
    }

}