package io.skygear.chatexample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_api_params.*

class ApiParamsActivity : AppCompatActivity() {

    private var adapter: ApiParamsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_params)
        title = fetchExtras().name

        initViews()
        initRecyclerView(fetchExtras())
    }

    private fun initViews() {
        continue_btn.setOnClickListener { submitParams() }
    }

    private fun fetchExtras(): ApiTask {
        return intent.getSerializableExtra(EXTRAS_KEY) as ApiTask
    }

    private fun initRecyclerView(apiTask: ApiTask) {
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = ApiParamsAdapter(this, apiTask)
        recycler_view.adapter = adapter
    }

    private fun submitParams() {
        val finalAdapter = adapter
        if(finalAdapter != null) {
            if(!finalAdapter.isAllParamsFilled()) {
                Toast.makeText(this, R.string.params_incomplete, Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, ApiTestActivity::class.java)
            intent.putExtra(EXTRAS_KEY, finalAdapter.getParams())
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRAS_KEY = "extras"
    }
}
