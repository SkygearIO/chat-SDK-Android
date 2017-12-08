package io.skygear.plugins.chat.ui

open interface ConnectionListener {
    fun onOpen(fragment: ConversationFragment)
    fun onClose(fragment: ConversationFragment)
    fun onError(fragment: ConversationFragment, e: Exception?)
}
