package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.Message

class MessageStatusText {
    var deliveringText: String
    var deliveredText: String
    var someReadText: String
    var allReadText: String

    constructor(deliveringText: String?, deliveredText: String?, someReadText: String?, allReadText: String?) {
        this.deliveringText = deliveringText ?: "Delivering"
        this.deliveredText = deliveredText ?: statusToString(Message.Status.DELIVERED)
        this.someReadText = someReadText ?: statusToString(Message.Status.SOME_READ)
        this.allReadText = allReadText  ?: statusToString(Message.Status.ALL_READ)
    }

    private fun statusToString(status: Message.Status): String {
        return status.getName().replace("_", " ", true).capitalize()

    }
}