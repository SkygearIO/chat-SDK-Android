package io.skygear.chatexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import io.skygear.skygear.*

class LogInActivity : AppCompatActivity() {
    private val LOG_TAG: String = "LogInActivity"

    private var mSkygear: Container? = null
    private var mUsernameEt: EditText? = null
    private var mPasswordEt: EditText? = null
    private var mLogInBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        mUsernameEt = findViewById(R.id.username_et) as EditText?
        mPasswordEt = findViewById(R.id.password_et) as EditText?
        mLogInBtn = findViewById(R.id.log_in_btn) as Button?
        mLogInBtn?.setOnClickListener {
            login(mUsernameEt?.text, mPasswordEt?.text)
        }

        mSkygear = Container.defaultContainer(this)
    }

    fun login(username: Editable?, password: Editable?) {
        if (username?.isNullOrBlank() == false && password?.isNullOrBlank() == false) {
            val loading = ProgressDialog(this)
            loading.setTitle(R.string.loading)
            loading.setMessage(getString(R.string.logging_in))
            loading.show()

            mSkygear?.auth?.loginWithUsername(username?.toString(), password?.toString(), object : AuthResponseHandler() {
                override fun onAuthSuccess(user: Record) {
                    loading.dismiss()
                    loginSuccess()
                }

                override fun onAuthFail(reason: Error) {
                    loading.dismiss()

                    loginFail()
                }
            })
        }
    }


    fun loginSuccess() {
        finish()
    }

    fun loginFail() {
        AlertDialog.Builder(this).setMessage(R.string.login_failed).show()
    }
}
