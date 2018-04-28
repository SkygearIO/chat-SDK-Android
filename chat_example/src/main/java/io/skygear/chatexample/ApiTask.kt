package io.skygear.chatexample

import org.json.JSONObject
import java.io.Serializable

/**
 * Created by camerash on 4/28/18.
 * Api Test Object
 */
class ApiTask(obj: JSONObject): Serializable {

    val name: String
    val params = mutableMapOf<String, String>()

    init{
        name = obj.getString(TASK_NAME_KEY)

        val array = obj.getJSONArray(PARAMS_KEY)
        for(i in 0 until array.length()) {
            params[array.getString(i)] = ""
        }
    }

    companion object {
        const val TASK_NAME_KEY = "name"
        const val PARAMS_KEY = "params"
    }

}