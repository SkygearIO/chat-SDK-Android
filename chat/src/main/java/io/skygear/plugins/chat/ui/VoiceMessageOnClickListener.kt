package io.skygear.chatkit.messages

import io.skygear.plugins.chat.ui.model.VoiceMessage

interface VoiceMessageOnClickListener {
    fun onVoiceMessageClick(voiceMessage: VoiceMessage)
}