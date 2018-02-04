package io.skygear.plugins.chat.ui

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso
import io.skygear.chatkit.commons.ImageLoader
import io.skygear.plugins.chat.ui.utils.AvatarBuilder

class DefaultAvatarImageLoader(
    val context: Context,
    val avatarBuilder: AvatarBuilder
) : ImageLoader {

    override fun loadImage(imageView: ImageView?, url: String?) {
        if (url == null) {
            return
        }

        if (this.avatarBuilder.isValidAvatarBuilderUri(url)) {
            val bm = this.avatarBuilder.avatarForUri(url)
            imageView?.setImageBitmap(bm)
            return
        }

        var creator = Picasso.with(this.context)
                .load(url)
        creator.fit().centerCrop()
        creator.into(imageView)
    }
}
