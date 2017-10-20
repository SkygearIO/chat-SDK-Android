package io.skygear.chatexample

import android.text.TextUtils
import io.skygear.skygear.SkygearApplication

class MainApp() : SkygearApplication() {
    override fun getSkygearEndpoint(): String? {
        return "https://carmenlau.staging.skygeario.com/"
    }

    override fun getApiKey(): String? {
        return "170f5913a7d44c8d8dfa21b44bca244b"
    }
}
