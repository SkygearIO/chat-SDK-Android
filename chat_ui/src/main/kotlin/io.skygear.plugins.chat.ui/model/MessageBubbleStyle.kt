package io.skygear.plugins.chat.ui.model

class MessageBubbleStyle {
    val backgroundColorForIncomingMessages: Int
    val backgroundColorForOutgoingMessages: Int
    constructor(backgroundColorForIncomingMessages: Int, backgroundColorForOutgoingMessages: Int) {
        this.backgroundColorForIncomingMessages = backgroundColorForIncomingMessages
        this.backgroundColorForOutgoingMessages = backgroundColorForOutgoingMessages
    }
}
