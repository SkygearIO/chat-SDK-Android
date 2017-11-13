package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.Message as ChatMessage
import android.net.Uri


class MessageFactory {
    companion object {
        fun getMessage(m: ChatMessage, style: MessageStyle, imageUri: Uri? = null) : Message {
            return m.asset?.mimeType.let {
                when {
                    it?.startsWith("image") == true -> ImageMessage(m, imageUri, style)
                    it?.equals(VoiceMessage.MIME_TYPE) == true -> VoiceMessage(m, style)
                    else -> Message(m, style)
                }
            }
        }
    }
}
