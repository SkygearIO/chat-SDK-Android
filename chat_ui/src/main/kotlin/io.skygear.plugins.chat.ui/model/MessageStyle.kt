package io.skygear.plugins.chat.ui.model


class MessageStyle {
    var hideOutgoing: Boolean
    var hideIncoming: Boolean
    var senderTextColor: Int
    var statusText: MessageStatusText
    var dateFormat: String
    var timeStyle: MessageTimeStyle
    var bubbleStyle: MessageBubbleStyle

    constructor(hideOutgoing: Boolean, hideIncoming: Boolean, senderTextColor: Int, statusText: MessageStatusText, dateFormat: String, timeStyle: MessageTimeStyle, bubbleStyle: MessageBubbleStyle) {
        this.hideOutgoing = hideOutgoing
        this.hideIncoming = hideIncoming
        this.senderTextColor = senderTextColor
        this.statusText = statusText
        this.dateFormat = dateFormat
        this.timeStyle = timeStyle
        this.bubbleStyle = bubbleStyle
    }
}