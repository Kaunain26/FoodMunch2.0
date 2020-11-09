package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.Validations

class ChangePassWordActivity : AppCompatActivity() {

    private lateinit var btnApply: Button
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var imgClose: ImageView
    private lateinit var progressBar: ProgressBar
    lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        /*Initializing views*/
        initView()

        imgClose.setOnClickListener {
            super.onBackPressed()
        }

        btnApply.setOnClickListener {
            updatePassword()
        }

    }

    private fun updatePassword() {
        val isValidated = validationUserInput()  // validating user input
        if (isValidated) {
            /* Hiding verify button and making progress bar visible
            *Its indicating that processes are in progress*/
            progressBar.visibility = View.VISIBLE
            btnApply.visibility = View.GONE

            /* Checking internet connection for sending json request*/
            if (ConnectionManager().isNetworkAvailable(this)) {

                mRef = FirebaseDatabase.getInstance().getReference("Users")
                mRef.child(intent?.getStringExtra("mobile_number")!!).child("password")
                    .setValue(etNewPassword.text.toString())
                    .addOnSuccessListener {

                        /*Successfully  password changed AlertDialog*/
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Success!!")
                        builder.setMessage("Your password is successfully changed")
                        builder.setIcon(R.drawable.ic_success)
                        builder.setPositiveButton("Got it!") { dialog, which ->
                            startActivity(Intent(this, LoginActivity::class.java))
                            ActivityCompat.finishAffinity(this)
                        }
                        builder.setCancelable(false)
                        builder.show()

                    }.addOnFailureListener {
                        /*Hiding progress bar and making verify button visible*/
                        progressBar.visibility = View.GONE
                        btnApply.visibility = View.VISIBLE

                        Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                    }

            } else {
                /* Hiding progress bar and making verify button visible, if there is no internet connection*/
                progressBar.visibility = View.GONE
                btnApply.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initView() {
        btnApply = findViewById(R.id.btnVerify)
        etNewPassword = findViewById(R.id.etNewPassword)
        imgClose = findViewById(R.id.imgClose)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    /*validating user input*/
    private fun validationUserInput(): Boolean {
        return if (Validations.validatePassword(etNewPassword.text.toString())) {
            etNewPassword.error = null

            if (Validations.matchPassword(
                    etNewPassword.text.toString(),
                    etConfirmPassword.text.toString()
                )
            ) {
                etConfirmPassword.error = null
                true  /* Return true if validation is successful*/
            } else {
                Toast.makeText(this, "Password did't match", Toast.LENGTH_SHORT).show()
                etNewPassword.error = "Password did't match"
                etConfirmPassword.error = "Password did't match"
                false /* Return false if validation is successful*/
            }
        } else {
            etNewPassword.error = "Invalid Password"
            false  /* Return false if validation is successful*/
        }
    }
}