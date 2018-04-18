package io.skygear.chatexample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import io.skygear.skygear.Container
import io.skygear.skygear.Record
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

class MainActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "MainActivity"

    private var mSkygear: Container? = null
    private var mSignUpBtn: Button? = null
    private var mLogInBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCenter.start(application,
                "37546527-c1b7-47ba-9eaa-f92ea8ee27f5",
                Analytics::class.java,
                Crashes::class.java)

        setContentView(R.layout.activity_main)

        mSignUpBtn = findViewById<Button>(R.id.sign_up_btn)
        mLogInBtn = findViewById<Button>(R.id.log_in_btn)

        mSignUpBtn?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        mLogInBtn?.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

        mSkygear = Container.defaultContainer(this)
    }

    override fun onResume() {
        super.onResume()

        val currentUser: Record? = mSkygear?.auth?.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, ConversationsActivity::class.java))

            finish()
        }
    }
}
