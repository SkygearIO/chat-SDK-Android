package io.skygear.chatexample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import io.skygear.skygear.Container
import kotlinx.android.synthetic.main.activity_api_list.*
import org.json.JSONArray
import java.nio.charset.Charset

class ApiListActivity : AppCompatActivity(), ApiListAdapter.ApiTaskClickListener {

    private val mSkygear: Container = Container.defaultContainer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_list)
        initViews()
        initRecyclerView(getApiTestList())
    }

    private fun initViews() {
        conversations_btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun getApiTestJson(): JSONArray {
        val inputStream = assets.open("apiTest.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        val jsonString = String(buffer, Charset.defaultCharset())
        return JSONArray(jsonString)
    }

    private fun getApiTestList(): ArrayList<ApiTask> {
        val testList = arrayListOf<ApiTask>()
        val jsonArray = getApiTestJson()
        (0 until jsonArray.length()).mapTo(testList) { ApiTask(jsonArray.getJSONObject(it)) }
        return testList
    }

    private fun initRecyclerView(apiList: ArrayList<ApiTask>) {
        val layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(recycler_view.context, layoutManager.orientation))
        recycler_view.adapter = ApiListAdapter(this, apiList)
    }

    override fun onApiTaskClicked(task: ApiTask) {
        try {
            Class.forName(packageName + ApiListActivity.API_TASK_PACKAGE + task.name.replace("\\s".toRegex(), ""))
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Error: Testing module not found", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ApiParamsActivity::class.java)
        intent.putExtra(ApiParamsActivity.EXTRAS_KEY, task)
        startActivity(intent)
    }

    companion object {
        const val API_TASK_PACKAGE = ".apitask."
    }
}
