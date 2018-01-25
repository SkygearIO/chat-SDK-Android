package io.skygear.plugins.chat.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.skygear.plugins.chat.Conversation
import io.skygear.plugins.chat.ui.model.User
import java.io.Serializable

open class ConversationViewAdapter : Serializable {
    var target: Target? = null

    open fun backgroundImageURL(conversation: Conversation): String? {
        return null
    }

    fun setBackground(view: ConversationView, conversation: Conversation, imageView: ImageView?) {
        val backgroundImageURL = this.backgroundImageURL(conversation)
        backgroundImageURL?.let {
            val picasso = Picasso.with(view.context)
            picasso.load(backgroundImageURL).into(imageView)
        }
    }

}