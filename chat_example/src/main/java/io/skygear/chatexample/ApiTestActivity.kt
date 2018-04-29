package io.skygear.chatexample

import android.os.Bundle
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
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
        try {
            val module = Class.forName(packageName + ApiListActivity.API_TASK_PACKAGE + apiTask.name.replace("\\s".toRegex(), "")).newInstance()
            if(module is ApiTestModule) {
                this.module = module
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Error: Testing module not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun loadCustomView() {
        val mod = module ?: return
        val view = mod.onLoadCustomView(this)
        if(view != null) {
            view_container.addView(view)
            fab.visibility = View.VISIBLE
            fab.show()
            setFabOnClickListener()
        } else {
            view_container.visibility = View.GONE
            fab.hide()
            fab.visibility = View.GONE
        }
    }

    private fun performApiTest(apiTask: ApiTask) {
        val mod = module ?: return
        mod.onApiTest(this, mSkygear, mChatContainer, apiTask, view_container)
    }

    private fun setFabOnClickListener() {
        fab.setOnClickListener {
            TransitionManager.beginDelayedTransition(root_linear_layout, TransitionSet().addTransition(ChangeBounds()))
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            val originalLayoutParams = view_container.layoutParams as LinearLayout.LayoutParams
            params1.weight = if(originalLayoutParams.weight == 0.5f) 1.0f else if(originalLayoutParams.weight == 1.0f) 0.0f else 0.5f
            view_container.layoutParams = params1
            val params2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            params2.weight = if(originalLayoutParams.weight == 0.5f) 0.0f else if(originalLayoutParams.weight == 1.0f) 1.0f else 0.5f
            log_fragment.view?.layoutParams = params2
        }
    }
}
