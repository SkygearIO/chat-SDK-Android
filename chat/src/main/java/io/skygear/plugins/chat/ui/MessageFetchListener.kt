package io.skygear.plugins.chat.ui

import io.skygear.plugins.chat.Message
import io.skygear.skygear.Error

open interface MessageFetchListener {
    fun onBeforeMessageFetch(fragment: ConversationFragment)
    fun onMessageFetchFailed(fragment: ConversationFragment, error: Error)
    fun onMessageFetchSuccess(fragment: ConversationFragment, messages: List<Message>)
}