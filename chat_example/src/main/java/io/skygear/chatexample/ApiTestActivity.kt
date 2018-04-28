package io.skygear.chatexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class ApiTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_test)
        title = fetchExtras().name

        loadTestModule(fetchExtras())
    }

    private fun fetchExtras(): ApiTask {
        return intent.getSerializableExtra(ApiParamsActivity.EXTRAS_KEY) as ApiTask
    }

    private fun loadTestModule(apiTask: ApiTask) {
        val module: Class<*>

        try {
            module = Class.forName(packageName + ApiListActivity.API_TASK_PACKAGE + apiTask.name.trim())
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return
        }

        
    }
}
