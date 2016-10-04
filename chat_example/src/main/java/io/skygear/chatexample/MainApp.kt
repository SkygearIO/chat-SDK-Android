package io.skygear.chatexample

import android.text.TextUtils
import io.skygear.skygear.SkygearApplication

class MainApp() : SkygearApplication() {
    override fun getSkygearEndpoint(): String? {
        val endpoint: String? = "ENDPOINT"

        if (TextUtils.isEmpty(endpoint))
            throw UnsupportedOperationException("not implemented")
        else
            return endpoint
    }

    override fun getApiKey(): String? {
        val key: String? = "KEY"

        if (TextUtils.isEmpty(key))
            throw UnsupportedOperationException("not implemented")
        else
            return key
    }
}
