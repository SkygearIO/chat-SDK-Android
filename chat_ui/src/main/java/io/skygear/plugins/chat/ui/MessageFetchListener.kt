package io.skygear.plugins.chat.ui

import io.skygear.plugins.chat.Message
import io.skygear.skygear.Error
import java.io.Serializable

open interface MessageFetchListener : Serializable {
    fun onBeforeMessageFetch(fragment: ConversationFragment)
    fun onMessageFetchFailed(fragment: ConversationFragment, error: Error)
    fun onMessageFetchSuccess(fragment: ConversationFragment, messages: List<Message>, isCached: Boolean)
}