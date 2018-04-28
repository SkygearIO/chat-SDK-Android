package io.skygear.chatexample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.skygear.skygear.Container
import kotlinx.android.synthetic.main.activity_api_list.*

class ApiListActivity : AppCompatActivity() {

    private val mSkygear: Container = Container.defaultContainer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_list)
        initViews()
    }

    private fun initViews() {
        conversations_btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
