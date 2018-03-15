package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.Message as ChatMessage
import android.net.Uri

class MessageFactory {
    companion object {
        fun getMessage(m: ChatMessage, style: MessageStyle, uri: Uri? = null, orientation: Int? = null): Message {
            return m.asset?.mimeType.let {
                when {
                    it?.startsWith("image") == true -> ImageMessage(m, uri, orientation, style)
                    it?.equals(VoiceMessage.MIME_TYPE) == true -> VoiceMessage(m, style, uri)
                    else -> Message(m, style)
                }
            }
        }
    }
}
