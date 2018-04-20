package io.skygear.plugins.chat.ui.model

import android.net.Uri
import android.media.MediaRecorder
import io.skygear.chatkit.commons.models.MessageContentType
import io.skygear.skygear.Asset
import io.skygear.skygear.Record
import io.skygear.plugins.chat.Message as ChatMessage

open class VoiceMessage : Message, MessageContentType {

    companion object {
        /**
         * Corresponding Format for {@link MediaRecorder.OutputFormat}.
         */
        val MEDIA_FORMAT = MediaRecorder.OutputFormat.MPEG_4

        /**
         * Corresponding Encoding for {@link MediaRecorder.AudioEncoder}.
         */
        val MEDIA_ENCODING = MediaRecorder.AudioEncoder.AAC

        val FILE_EXTENSION_NAME = "m4a"
        val MIME_TYPE = "audio/m4a"
        var AttachmentFieldName = "attachment"
        var DurationMatadataName = "length"

        fun isVoiceMessage(record: Record?): Boolean {
            if (record == null) return false

            val attachment = record.get(VoiceMessage.AttachmentFieldName) as? Asset ?: return false
            return attachment.mimeType == VoiceMessage.MIME_TYPE
        }

        fun isVoiceMessage(chatMsg: ChatMessage) = VoiceMessage.isVoiceMessage(chatMsg.record)

        fun isVoiceMessage(msg: Message) = VoiceMessage.isVoiceMessage(msg.chatMessage)
    }

    val attachment
        get() = this.chatMessage.record.get(VoiceMessage.AttachmentFieldName) as Asset

    val attachmentUrl
        get() = this.uri?.toString() ?: this.attachment.url

    val duration
        get() = this.chatMessage.metadata!!.getInt(VoiceMessage.DurationMatadataName)

    var state = VoiceMessage.State.INITIAL
    var uri: Uri? = null

    constructor(chatMsg: ChatMessage, style: MessageStyle, u: Uri? = null): super(chatMsg, style) {
        if (! VoiceMessage.isVoiceMessage(this.chatMessage)) {
            throw IllegalArgumentException("Not compatible skygear record")
        }
        this.uri = u
    }

    enum class State {
        INITIAL, PLAYING, PAUSED, PREPARING
    }
}
