package io.skygear.chatexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.skygear.chatexample.apitask.ApiTestModule
import io.skygear.chatexample.logger.Log
import io.skygear.chatexample.logger.LogFragment
import io.skygear.chatexample.logger.LogWrapper
import io.skygear.chatexample.logger.MessageOnlyLogFilter
import io.skygear.plugins.chat.ChatContainer
import io.skygear.skygear.Container
import kotlinx.android.synthetic.main.activity_api_test.*

class ApiTestActivity : AppCompatActivity() {

    var module: ApiTestModule? = null
    val mSkygear: Container = Container.defaultContainer(this)
    val mChatContainer: ChatContainer

    init{
        mChatContainer = ChatContainer.getInstance(mSkygear)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_test)
        title = fetchExtras().name

        initializeLogging()
        loadTestModule(fetchExtras())
        loadCustomView()
        performApiTest(fetchExtras())
    }

    private fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.logNode = logWrapper

        // Filter strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On screen logging via a fragment with a TextView.
        val logFragment = log_fragment as LogFragment
        msgFilter.next = logFragment.logView

        Log.i(javaClass.simpleName, "Ready")
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

        if(module is ApiTestModule) {
            this.module = module
        }
    }

    private fun loadCustomView() {
        val mod = module ?: return
        val view = mod.onLoadCustomView(this)
        if(view != null) {
            view_container.addView(view)
        } else {
            view_container.visibility = View.GONE
        }
    }

    private fun performApiTest(apiTask: ApiTask) {
        val mod = module ?: return
        mod.onApiTest(this, mSkygear, mChatContainer, apiTask, view_container)
    }
}
