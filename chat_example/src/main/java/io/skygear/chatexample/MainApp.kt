package io.skygear.chatexample

import android.text.TextUtils
import io.skygear.skygear.SkygearApplication

class MainApp() : SkygearApplication() {
    override fun getSkygearEndpoint(): String? {
        return "http://skygear.dev/"
    }

    override fun getApiKey(): String? {
        return "changeme"
    }
}
