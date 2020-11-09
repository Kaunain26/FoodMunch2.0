package com.knesarCreation.foodmunch.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.CartRecyclerAdapter
import com.knesarCreation.foodmunch.adapter.ResMenuRecyclerAdapter
import com.knesarCreation.foodmunch.database.CartEntity
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.model.CartItems
import com.knesarCreation.foodmunch.model.FoodItems
import com.knesarCreation.foodmunch.model.OrderHistory
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class CartActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var recyclerCart: RecyclerView
    lateinit var cartRecyclerAdapter: CartRecyclerAdapter
    private lateinit var txtResName: TextView
    lateinit var btnPlaceOrder: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager
    lateinit var rlLoading: RelativeLayout
    private var resId: Int? = 0
    private var resName: String? = ""
    var totalCost = 0
    var orderList = arrayListOf<CartItems>()
    private val foodItemsList = arrayListOf<FoodItems>()
    lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        /*Finding view by id*/
        initViews()

        /* Initializing session manager for saving user's data in SHARED PREFs */
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        /*getting intent value*/
        resId = intent?.getIntExtra("res_id", 0)
        resName = intent?.getStringExtra("res_name")
        txtResName.text = resName

        /*Setting up toolbar*/
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Getting data from database and adding into a list*/
        setUpCart()
        /*Placing order*/
        placeOrder()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.cartToolbar)
        txtResName = findViewById(R.id.txtResName)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        recyclerCart = findViewById(R.id.cartRecycler)
        rlLoading = findViewById(R.id.rlLoading)
    }

    private fun placeOrder() {
        for (i in 0 until orderList.size) {
            totalCost += orderList[i].costForOne.toInt()
            Log.d("tag", "$orderList")
        }
        btnPlaceOrder.text = "Place Order(Total: Rs.$totalCost)"
        btnPlaceOrder.setOnClickListener {
            for (i in orderList) {
                foodItemsList.add(FoodItems(i.id.toString(), i.name, i.costForOne))
            }
            val orderHistory = OrderHistory(
                System.currentTimeMillis().toInt(),
                resName!!,
                totalCost.toString(),
                formatDate()!!,
                foodItemsList
            )

            /* Hiding btnPlaceOrder  and making visible rlLoading
             * Its indicating that processes are going on*/
            rlLoading.visibility = View.VISIBLE
            btnPlaceOrder.visibility = View.GONE

            /*Checking internet connection*/
            if (ConnectionManager().isNetworkAvailable(this)) {

                val userId = sharedPreferences.getString("mobile_number", null)

                /*Adding data to Firebase RealTime Database*/
                mRef = FirebaseDatabase.getInstance().getReference("OrderHistory").child(userId!!)
                mRef.push().setValue(orderHistory).addOnSuccessListener {

                    /*Clearing cart after order is placed*/
                    val isClear = _root_ide_package_.com.knesarCreation.foodmunch.activity.CartActivity.ClearCart(
                        this,
                        resId!!
                    ).execute().get()
                    ResMenuRecyclerAdapter.isCartEmpty = true

                    /*after successfully cleared show a Dialog confirmation message that **ORDER IS PLACED***/
                    if (isClear) {
                        val dialog = Dialog(
                            this,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.dilaog_order_confirmation)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            rlLoading.visibility = View.VISIBLE
                            btnPlaceOrder.visibility = View.GONE

                            /*After a successful order open a Dashboard Activity*/
                            startActivity(
                                Intent(
                                    this,
                                    _root_ide_package_.com.knesarCreation.foodmunch.activity.DashboardActivity::class.java
                                )
                            )
                            ActivityCompat.finishAffinity(this)
                        }

                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            } else {
                /* Hiding rlLoading  and making btnPlaceOrder visible
             if there is no internet connection  */
                rlLoading.visibility = View.GONE
                btnPlaceOrder.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpCart() {
        val cartList = _root_ide_package_.com.knesarCreation.foodmunch.activity.CartActivity.GetOrderListFromDBAsync(
            applicationContext
        ).execute().get()

        for (order in cartList) {
            orderList.addAll(
                Gson().fromJson(order.foodItems, Array<CartItems>::class.java).asList()
            )
        }
        val linearLayout = LinearLayoutManager(this)
        recyclerCart.layoutManager = linearLayout
        cartRecyclerAdapter = CartRecyclerAdapter(this, orderList)
        recyclerCart.adapter = cartRecyclerAdapter
    }

    private fun formatDate(): String? {
        val c = Calendar.getInstance().time
        Log.d("Formatted", "Current time => $c")

        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        Log.d("Formatted", "Current time => $formattedDate")

        return formattedDate
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return true
    }

    class GetOrderListFromDBAsync(context: Context) : AsyncTask<Void, Void, List<CartEntity>>() {

        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): List<CartEntity> {
            return db.cartDao().getAllItems()
        }
    }

    class ClearCart(context: Context, private val resId: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.cartDao().deleteOrder(resId)
            db.close()
            return true
        }
    }
}