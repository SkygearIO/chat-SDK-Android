package io.skygear.plugins.chat.ui.model


class MessageStyle {
    var hideOutgoing: Boolean
    var hideIncoming: Boolean
    var senderTextColor: Int
    var statusText: MessageStatusText
    var dateFormat: String

    constructor(hideOutgoing: Boolean, hideIncoming: Boolean, senderTextColor: Int, statusText: MessageStatusText, dateFormat: String) {
        this.hideOutgoing = hideOutgoing
        this.hideIncoming = hideIncoming
        this.senderTextColor = senderTextColor
        this.statusText = statusText
        this.dateFormat = dateFormat
    }
}