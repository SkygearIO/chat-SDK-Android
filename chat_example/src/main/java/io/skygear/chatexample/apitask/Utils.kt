package io.skygear.chatexample.apitask

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.skygear.skygear.Container

class Utils {
    companion object {
        fun isLoggedIn(context: Context): Boolean {
            return Container.defaultContainer(context).auth.currentUser != null
        }

        fun copyToClipboard(context:Context, tag: String, string: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(tag, string)
            clipboard.primaryClip = clip
        }
    }
}