package io.skygear.plugins.chat.ui.model


class MessageStyle {
    var hideOutgoing: Boolean
    var hideIncoming: Boolean
    var senderTextColor: Int

    constructor(hideOutgoing: Boolean, hideIncoming: Boolean, senderTextColor: Int) {
        this.hideOutgoing = hideOutgoing
        this.hideIncoming = hideIncoming
        this.senderTextColor = senderTextColor
    }
}