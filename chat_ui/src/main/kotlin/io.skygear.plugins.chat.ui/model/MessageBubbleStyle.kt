package io.skygear.plugins.chat.ui.model

class MessageBubbleStyle {
    val backgroundColorForIncomingMessages: Int
    val backgroundColorForOutgoingMessages: Int
    val textColorForIncomingMessages: Int
    val textColorForOutgoingMessages: Int
    constructor(backgroundColorForIncomingMessages: Int, backgroundColorForOutgoingMessages: Int, textColorForIncomingMessages: Int, textColorForOutgoingMessages: Int) {
        this.backgroundColorForIncomingMessages = backgroundColorForIncomingMessages
        this.backgroundColorForOutgoingMessages = backgroundColorForOutgoingMessages
        this.textColorForIncomingMessages = textColorForIncomingMessages
        this.textColorForOutgoingMessages = textColorForOutgoingMessages
    }
}
