package io.skygear.chatexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import io.skygear.skygear.AuthResponseHandler
import io.skygear.skygear.Container
import io.skygear.skygear.Error
import io.skygear.skygear.User

class SignUpActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "SignUpActivity"

    private var mSkygear: Container? = null
    private var mUsernameEt: EditText? = null
    private var mPasswordEt: EditText? = null
    private var mSignUpBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mUsernameEt = findViewById(R.id.username_et) as EditText?
        mPasswordEt = findViewById(R.id.password_et) as EditText?
        mSignUpBtn = findViewById(R.id.sign_up_btn) as Button?
        mSignUpBtn?.setOnClickListener {
            signup(mUsernameEt?.text, mPasswordEt?.text)
        }

        mSkygear = Container.defaultContainer(this)
    }

    fun signup(username: Editable?, password: Editable?) {
        if (username?.isNullOrBlank() == false && password?.isNullOrBlank() == false) {
            val loading = ProgressDialog(this)
            loading.setTitle(R.string.loading)
            loading.setMessage(getString(R.string.signing_up))
            loading.show()

            mSkygear?.signupWithUsername(username?.toString(), password?.toString(), object : AuthResponseHandler() {
                override fun onAuthSuccess(user: User) {
                    loading.dismiss()
                    signupSuccess()
                }

                override fun onAuthFail(reason: Error) {
                    loading.dismiss()

                    signupFail()
                }
            })
        }
    }


    fun signupSuccess() {
        finish()
    }

    fun signupFail() {
        AlertDialog.Builder(this).setMessage(R.string.signup_failed).show()
    }
}
