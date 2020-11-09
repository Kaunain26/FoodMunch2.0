package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.model.Users
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import com.knesarCreation.foodmunch.util.Validations
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    private lateinit var txtSignUp: TextView
    lateinit var progressBar: ProgressBar
    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mRef: DatabaseReference
    var isEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*initializing views*/
        initViews()

        /* Initializing session manager for saving user's data in SHARED PREFs */
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        /*Checking user logged in or not*/
        val isLoggedIn = sessionManager.isLoggedIn()
        if (isLoggedIn) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            validateUserInput()
        }

        txtForgotPassword.setOnClickListener {
            if (isEnabled) {      // Preventing multiple click
                isEnabled = false  /* Disable clicks by setting isEnable false*/
                startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
            }
        }

        txtSignUp.setOnClickListener {
            if (isEnabled) { // Preventing multiple click
                isEnabled = false  /*Disable clicks by setting isEnable false*/
                startActivity(Intent(this@LoginActivity, RegisterUserActivity::class.java))
            }
        }
    }

    private fun initViews() {
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etNewPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtSignUp = findViewById(R.id.txtSignUp)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun validateUserInput() {
        if (Validations.validatePhoneNo(etMobileNumber.text.toString()) && Validations.validatePassword(
                etPassword.text.toString()
            )
        ) {
            etMobileNumber.error = null
            etPassword.error = null

            /*checking user credentials*/
            authenticateUser()

        } else {
            Toast.makeText(this, "Invalid Phone and Password", Toast.LENGTH_SHORT).show()
            etMobileNumber.error = "Invalid Phone and Password"
            etPassword.error = "Invalid Phone and Password"
        }
    }

    private fun authenticateUser() {
        progressBar.visibility = View.VISIBLE
        btnLogin.visibility = View.GONE

        mRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(etMobileNumber.text.toString())

        mRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val value = snapshot.getValue(Users::class.java)
                    val password = value?.password

                    /*saving user data in Shared prefs*/
                    savingUserDataInPrefs(value!!)

                    if (password.equals(etPassword.text.toString())) {
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        progressBar.visibility = View.GONE
                        btnLogin.visibility = View.VISIBLE
                        Toast.makeText(this@LoginActivity, "Incorrect Password", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    btnLogin.visibility = View.VISIBLE
                    Toast.makeText(this@LoginActivity, "No user exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                btnLogin.visibility = View.VISIBLE
                Toast.makeText(
                    this@LoginActivity,
                    "$error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun savingUserDataInPrefs(value: Users) {
        /*saving users details in Shared prefs for further use*/
        sharedPreferences.edit()
            .putString("userId", value.mobileNo)
            .apply() /*saving mobile number as a user Id*/

        sharedPreferences.edit()
            .putString("name", value.userName).apply()

        sharedPreferences.edit()
            .putString("email", value.email).apply()

        sharedPreferences.edit()
            .putString("mobile_number", value.mobileNo)
            .apply()

        sharedPreferences.edit()
            .putString("address", value.deliveryAddress).apply()

        sessionManager.login(true)

    }

    private fun authenticateLogin() {

        /* Hiding login button and making visible progress bar
            * Its indicating that processes are going on*/
        progressBar.visibility = View.VISIBLE
        btnLogin.visibility = View.GONE

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/login/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", etMobileNumber.text.toString())
        jsonParams.put("password", etPassword.text.toString())

        /* Checking internet connection*/
        if (ConnectionManager().isNetworkAvailable(this)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        if (success) {
                            /*if success is true we are getting user data from server*/
                            val jsonObject = data.getJSONObject("data")

                            /*saving users details in Shared prefs for further use*/
                            sharedPreferences.edit()
                                .putString("userId", jsonObject.getString("user_id")).apply()

                            sharedPreferences.edit()
                                .putString("name", jsonObject.getString("name")).apply()

                            sharedPreferences.edit()
                                .putString("email", jsonObject.getString("email")).apply()

                            sharedPreferences.edit()
                                .putString("mobile_number", jsonObject.getString("mobile_number"))
                                .apply()

                            sharedPreferences.edit()
                                .putString("address", jsonObject.getString("address")).apply()

                            sessionManager.login(true)

                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        } else {
                            /* Hiding progress bar and making login button visible
                             *  here its indicating that Something  went wrong*/
                            progressBar.visibility = View.GONE
                            btnLogin.visibility = View.VISIBLE

                            /*if success is false we display error message*/
                            val errorMessage = data.getString("errorMessage")
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        /*Catching Error if any JSON exception occurred*/

                        /* Hiding progress bar and making  login button visible*/
                        progressBar.visibility = View.GONE
                        btnLogin.visibility = View.VISIBLE
                        Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {
                    /*Catching Error if any VolleyError occurred*/

                    /* Hiding progress bar and making login button visible*/
                    progressBar.visibility = View.GONE
                    btnLogin.visibility = View.VISIBLE

                    Toast.makeText(this, "VolleyError: $it", Toast.LENGTH_LONG).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "c28e4d98687310"
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)

        } else {
            /* Hiding progress bar and making  login button visible if there is no internet connection  */
            progressBar.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE

            /*Alerting user that there is no internet connection enabled*/
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Connection!!")
            builder.setMessage("Internet connection is not enable. Please connect to the internet")
            builder.setPositiveButton("OK") { dialog, which ->
                val openSetting =
                    /* using explicit intent for opening setting */
                    Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                startActivity(openSetting)
            }
            builder.show()
        }
    }

    override fun onResume() {
        isEnabled = true   // Enable clicks by setting isEnable false
        super.onResume()
    }
}