package io.skygear.chatexample

import org.json.JSONObject
import java.io.Serializable

class ApiTask(obj: JSONObject): Serializable {

    val name: String
    val loginRequired: Boolean
    val params = mutableMapOf<String, String>()

    init{
        name = obj.getString(TASK_NAME_KEY)
        loginRequired = obj.getBoolean(LOGIN_REQUIRED_KEY)

        val array = obj.getJSONArray(PARAMS_KEY)
        for(i in 0 until array.length()) {
            params[array.getString(i)] = ""
        }
    }

    companion object {
        const val TASK_NAME_KEY = "name"
        const val LOGIN_REQUIRED_KEY = "loginRequired"
        const val PARAMS_KEY = "params"
    }

}