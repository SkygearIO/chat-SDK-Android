package io.skygear.chatexample.apitask

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.skygear.chatexample.ApiTask
import io.skygear.skygear.Container

class Utils {
    companion object {
        const val PACKAGE_SEPARATOR = '.'

        fun isLoggedIn(context: Context): Boolean {
            return Container.defaultContainer(context).auth.currentUser != null
        }

        fun copyToClipboard(context:Context, tag: String, string: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(tag, string)
            clipboard.primaryClip = clip
        }

        fun getModuleByTask(apiTask: ApiTask): ApiTestModule {
            val module = Class.forName( Utils::class.java.`package`.name + PACKAGE_SEPARATOR + apiTask.name.replace("\\s".toRegex(), "")).newInstance()
            if(module is ApiTestModule) return module
            else throw ClassNotFoundException("Error: Testing module not found")
        }
    }
}