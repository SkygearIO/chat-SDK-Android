package io.skygear.chatexample

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.skygear.chatexample.apitask.Utils
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import io.skygear.skygear.LogoutResponseHandler
import kotlinx.android.synthetic.main.activity_api_list.*
import org.json.JSONArray
import java.nio.charset.Charset

class ApiListActivity : AppCompatActivity(), ApiListAdapter.ApiTaskClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_list)
        title = getString(R.string.api_test_list)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.api_list, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val logoutBtn = menu?.findItem(R.id.log_out_menu)
        logoutBtn?.isVisible = (Utils.isLoggedIn(this))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.log_out_menu -> {
                confirmLogOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    private fun confirmLogOut() {
        AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.are_your_sure_to_log_out)
                .setPositiveButton(R.string.yes) { dialog, which -> logOut() }
                .setNegativeButton(R.string.no, null).show()
    }

    private fun logOut() {
        val loading = ProgressDialog(this)
        loading.setTitle(R.string.loading)
        loading.setMessage(getString(R.string.logging_out))
        loading.show()

        Container.defaultContainer(this).auth.logout(object : LogoutResponseHandler() {
            override fun onLogoutSuccess() {
                invalidateOptionsMenu()
                loading.dismiss()
            }

            override fun onLogoutFail(error: Error) {
                loading.dismiss()
                logoutFail()
            }
        })
    }

    private fun logoutFail() {
        AlertDialog.Builder(this).setTitle(R.string.logout_failed).show()
    }

    companion object {
        const val API_TASK_PACKAGE = ".apitask."
    }
}
