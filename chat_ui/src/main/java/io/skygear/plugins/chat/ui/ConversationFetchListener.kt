package io.skygear.plugins.chat.ui

import io.skygear.plugins.chat.Conversation
import io.skygear.skygear.Error
import java.io.Serializable

open interface ConversationFetchListener : Serializable {
    fun onBeforeConversationFetch(fragment: ConversationFragment)
    fun onConversationFetchFailed(fragment: ConversationFragment, error: Error)
    fun onConversationFetchSuccess(fragment: ConversationFragment, conversation: Conversation?)
}