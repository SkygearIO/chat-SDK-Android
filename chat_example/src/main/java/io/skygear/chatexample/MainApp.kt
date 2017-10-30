package io.skygear.chatexample

import android.text.TextUtils
import io.skygear.skygear.SkygearApplication

class MainApp() : SkygearApplication() {
    override fun getSkygearEndpoint(): String? {
        return "https://chatdemoapp.skygeario.com/"
    }

    override fun getApiKey(): String? {
        return "c0d796f60a9649d78ade26e65c460459"
    }
}
