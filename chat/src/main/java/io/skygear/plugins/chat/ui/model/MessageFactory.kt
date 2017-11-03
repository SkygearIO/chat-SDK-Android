package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.Message as ChatMessage


class MessageFactory {
    companion object {
        fun getMessage(m: ChatMessage) : Message {
            return m.asset?.mimeType.let {
                when {
                    it?.startsWith("image") == true -> ImageMessage(m)
                    it?.equals(VoiceMessage.MIME_TYPE) == true -> VoiceMessage(m)
                    else -> Message(m)
                }
            }
        }
    }
}
