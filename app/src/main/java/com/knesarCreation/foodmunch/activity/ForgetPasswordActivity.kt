package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.Validations


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var imgClose: ImageView
    private lateinit var etMobileNumber: EditText
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        /*initializing Views*/
        initViews()

        /*setting toolbar*/
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""  // setting toolbar title null

        /*closing activity*/
        imgClose.setOnClickListener {
            super.onBackPressed()
        }

        btnNext.setOnClickListener {
            /*verify user existence in firebase database*/
            verifyUserExistence()
        }
    }

    private fun initViews() {
        etMobileNumber = findViewById(R.id.etMobileNumber)
        btnNext = findViewById(R.id.btnNext)
        toolbar = findViewById(R.id.customToolbar)
        imgClose = findViewById(R.id.imgClose)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun verifyUserExistence() {
        val isValidated = validationUserInput()  // validating user input

        if (isValidated) {
            progressBar.visibility = View.VISIBLE
            btnNext.visibility = View.GONE

            /*checking connection manager*/
            if (ConnectionManager().isNetworkAvailable(this)) {

                /*checking user in firebase database*/
                val checkUser = FirebaseDatabase.getInstance().getReference("Users")
                    .orderByChild("mobileNo").equalTo(etMobileNumber.text.toString())
                checkUser.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            /* Hiding progress bar and making  visible next button*/
                            progressBar.visibility = View.GONE
                            btnNext.visibility = View.VISIBLE
                            val intent = Intent(
                                this@ForgetPasswordActivity,
                                ChangePassWordActivity::class.java
                            )
                            intent.putExtra("mobile_number", etMobileNumber.text.toString())
                            startActivity(intent)

                        } else {
                            /* Hiding progress bar and making  visible next button*/
                            progressBar.visibility = View.GONE
                            btnNext.visibility = View.VISIBLE

                            Toast.makeText(
                                this@ForgetPasswordActivity,
                                "No such user exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        /* Hiding progress bar and making  visible next button*/
                        progressBar.visibility = View.GONE
                        btnNext.visibility = View.VISIBLE

                        Toast.makeText(this@ForgetPasswordActivity, "$error", Toast.LENGTH_SHORT)
                            .show()
                    }

                })

            } else {
                /* Hiding progress bar and making  visible next button if there is no internet*/

                progressBar.visibility = View.GONE
                btnNext.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validationUserInput(): Boolean {
        return if (Validations.validatePhoneNo(etMobileNumber.text.toString())) {
            etMobileNumber.error = null
            true
        } else {
            etMobileNumber.error = "Invalid Mobile Number"
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}