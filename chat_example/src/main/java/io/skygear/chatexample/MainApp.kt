package io.skygear.chatexample

import android.text.TextUtils
import io.skygear.skygear.SkygearApplication

class MainApp() : SkygearApplication() {
    override fun getSkygearEndpoint(): String? {
        return "http://192.168.2.125:3000/"
    }

    override fun getApiKey(): String? {
        return "secretOURD"
    }
}
