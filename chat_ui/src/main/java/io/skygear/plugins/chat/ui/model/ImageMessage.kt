package io.skygear.plugins.chat.ui.model

import android.net.Uri
import io.skygear.chatkit.commons.models.MessageContentType
import io.skygear.plugins.chat.Message as ChatMessage

class ImageMessage : Message,
        MessageContentType.Image {

    val chatMessageImageUrl: String?
    var thumbnail: String? = null
    var width: Int? = null
    var height: Int? = null
    var orientation: Int? = null

    constructor(m: ChatMessage, imageUri: Uri?, orientation: Int?, style: MessageStyle) : super(m, style) {
        this.chatMessageImageUrl = this.chatMessage.asset?.url ?: imageUri?.toString()
        this.chatMessage.metadata?.let {
            if (it.has("thumbnail")) {
                this.thumbnail = it.getString("thumbnail")
            }

            if (it.has("width")) {
                this.width = it.getInt("width")
            }

            if (it.has("height")) {
                this.height = it.getInt("height")
            }
        }

        this.orientation = orientation
    }

    // Return null for image url to skip the default image loading of chatkit
    // Customized the image loading with base64 thumbnail
    // see onBind of IncomingImageMessageView.kt
    override fun getImageUrl(): String? = null
}
