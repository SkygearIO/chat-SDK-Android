package io.skygear.plugins.chat.ui.utils

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader

class ImageLoader(
        val context: Context,
        val avatarBuilder: AvatarBuilder
): ImageLoader {

    constructor(context: Context): this(context, AvatarBuilder.defaultBuilder())

    override fun loadImage(imageView: ImageView?, url: String?) {
        if (url == null) {
            return
        }

        // Load from avatar builder
        if (this.avatarBuilder.isValidAvatarBuilderUri(url)) {
            val bm = this.avatarBuilder.avatarForUri(url)
            imageView?.setImageBitmap(bm)
            return
        }

        Picasso.with(this.context).load(url).into(imageView)
    }
}
