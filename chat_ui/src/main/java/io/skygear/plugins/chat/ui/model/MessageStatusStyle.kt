package io.skygear.plugins.chat.ui.model

class MessageStatusStyle {
    val incomingTextColor: Int
    val outgoingTextColor: Int
    val enabled: Boolean

    constructor(incomingTextColor: Int, outgoingTextColor: Int, enabled: Boolean) {
        this.incomingTextColor = incomingTextColor
        this.outgoingTextColor = outgoingTextColor
        this.enabled = enabled
    }
}