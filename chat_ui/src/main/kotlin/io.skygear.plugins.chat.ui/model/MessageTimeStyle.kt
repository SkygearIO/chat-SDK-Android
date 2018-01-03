package io.skygear.plugins.chat.ui.model

class MessageTimeStyle {
    val incomingTextColor: Int
    val outgoingTextColor: Int

    constructor(incomingTextColor: Int, outgoingTextColor: Int) {
        this.incomingTextColor = incomingTextColor
        this.outgoingTextColor = outgoingTextColor
    }
}
