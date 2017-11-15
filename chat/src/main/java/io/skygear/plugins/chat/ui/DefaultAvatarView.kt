package io.skygear.plugins.chat.ui

import com.stfalcon.chatkit.utils.ShapeImageView
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import io.skygear.plugins.chat.ui.model.Message
import io.skygear.plugins.chat.ui.utils.AvatarBuilder
import io.skygear.plugins.chat.ui.utils.ImageLoader

class DefaultAvatarView(context: Context, attributeSet: AttributeSet): ShapeImageView(context, attributeSet) {

    var imageLoader: ImageLoader? = null

    init {

    }

    fun onBind(message: Message) {
        imageLoader = ImageLoader(context, AvatarBuilder())
        imageLoader?.loadImage(this, message.user?.avatar)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }
}