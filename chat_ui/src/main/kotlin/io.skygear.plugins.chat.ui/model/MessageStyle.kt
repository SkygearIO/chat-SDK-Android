package io.skygear.plugins.chat.ui.model

import io.skygear.plugins.chat.ui.AvatarType


class MessageStyle {
    var showSender: Boolean
    var showReceiver: Boolean
    var senderTextColor: Int


    constructor(showSender: Boolean, showReceiver: Boolean, senderTextColor: Int) {
        this.showSender = showSender
        this.showReceiver = showReceiver
        this.senderTextColor = senderTextColor
    }
}