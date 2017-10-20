package io.skygear.plugins.chat.ui.model

import io.skygear.skygear.Record
import org.json.JSONException
import io.skygear.plugins.chat.Message as ChatMessage

/**
 * Created by carmenlau on 10/15/17.
 */

class MessageFactory {
    companion object {
        fun getMessage(record: Record) : Message {
            try {
                val chatMessage = ChatMessage.fromJson(record.toJson())
                return MessageFactory.getMessage(chatMessage)
            } catch (e: JSONException) {
                throw IllegalArgumentException("Cannot serialize the skygear record")
            }
        }

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
