package io.skygear.plugins.chat.ui.model

class MessageStyle {
    var hideOutgoing: Boolean
    var hideIncoming: Boolean
    var senderTextColor: Int
    var statusText: MessageStatusText
    var dateFormat: String
    var timeStyle: MessageTimeStyle
    var statusStyle: MessageStatusStyle
    var bubbleStyle: MessageBubbleStyle
    val voiceMessageStyle: VoiceMessageStyle

    constructor(hideOutgoing: Boolean, hideIncoming: Boolean, senderTextColor: Int, statusText: MessageStatusText, dateFormat: String, timeStyle: MessageTimeStyle, bubbleStyle: MessageBubbleStyle, voiceMessageStyle: VoiceMessageStyle, statusStyle: MessageStatusStyle) {
        this.hideOutgoing = hideOutgoing
        this.hideIncoming = hideIncoming
        this.senderTextColor = senderTextColor
        this.statusText = statusText
        this.dateFormat = dateFormat
        this.timeStyle = timeStyle
        this.bubbleStyle = bubbleStyle
        this.voiceMessageStyle = voiceMessageStyle
        this.statusStyle = statusStyle
    }
}