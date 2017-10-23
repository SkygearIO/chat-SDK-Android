package io.skygear.chatexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import io.skygear.skygear.*


class SignUpActivity : AppCompatActivity() {
    private val LOG_TAG: String? = "SignUpActivity"

    private var mSkygear: Container? = null
    private var mUsernameEt: EditText? = null
    private var mPasswordEt: EditText? = null
    private var mSignUpBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mUsernameEt = findViewById<EditText>(R.id.username_et)
        mPasswordEt = findViewById<EditText>(R.id.password_et)
        mSignUpBtn = findViewById<Button>(R.id.sign_up_btn)
        mSignUpBtn?.setOnClickListener {
            signup(mUsernameEt?.text, mPasswordEt?.text)
        }

        mSkygear = Container.defaultContainer(this)
    }

    fun signup(username: Editable?, password: Editable?) {
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            val loading = ProgressDialog(this)
            loading.setTitle(R.string.loading)
            loading.setMessage(getString(R.string.signing_up))
            loading.show()


            val authFail = fun (_: Error?) {
                loading.dismiss()
                this@SignUpActivity.signupFail()
            }

            val authSuccess = fun (userRecord: Record, username: String) {
                userRecord.set("name", username)
                this@SignUpActivity.mSkygear?.publicDatabase?.save(
                        userRecord,
                        object : RecordSaveResponseHandler() {
                            override fun onPartiallySaveSuccess(
                                    successRecords: MutableMap<String, Record>?,
                                    errors: MutableMap<String, Error>?
                            ) = Unit

                            override fun onSaveSuccess(records: Array<out Record>?) {
                                if (records != null && records.isNotEmpty()) {
                                    loading.dismiss()
                                    this@SignUpActivity.signupSuccess()
                                }
                            }

                            override fun onSaveFail(error: Error?) = authFail(error)

                        })
                return
            }

            mSkygear?.auth?.signupWithUsername(
                    username.toString(),
                    password.toString(),
                    object : AuthResponseHandler() {
                        override fun onAuthSuccess(user: Record)
                                = authSuccess(user, username.toString())

                        override fun onAuthFail(reason: Error)
                                = authFail(reason)
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
