package com.stfalcon.chatkit.messages

import io.skygear.plugins.chat.ui.model.VoiceMessage

interface VoiceMessageOnClickListener {
    fun onClick(message: VoiceMessage)
}