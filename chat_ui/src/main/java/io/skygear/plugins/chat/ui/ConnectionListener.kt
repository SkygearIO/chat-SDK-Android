package io.skygear.plugins.chat.ui

import java.io.Serializable

open interface ConnectionListener : Serializable {
    fun onOpen(fragment: ConversationFragment)
    fun onClose(fragment: ConversationFragment)
    fun onError(fragment: ConversationFragment, e: Exception?)
}
