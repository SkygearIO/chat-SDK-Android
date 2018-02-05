package io.skygear.plugins.chat.ui

import io.skygear.plugins.chat.Message
import io.skygear.skygear.Error
import java.io.Serializable

open interface MessageSentListener : Serializable {
    fun onBeforeMessageSent(fragment: ConversationFragment, message: Message)
    fun onMessageSentFailed(fragment: ConversationFragment, message: Message?, error: Error)
    fun onMessageSentSuccess(fragment: ConversationFragment, message: Message)
}