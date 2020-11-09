package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.firebase.database.*
import com.google.gson.Gson
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.ResMenuRecyclerAdapter
import com.knesarCreation.foodmunch.database.CartEntity
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.model.RestaurantMenu
import com.knesarCreation.foodmunch.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_dashboard.*

class RestaurantMenuFragment : Fragment() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var resDetailsRecyclerView: RecyclerView
    lateinit var resMenuRecyclerAdapter: ResMenuRecyclerAdapter
    lateinit var rlLoading: RelativeLayout
    lateinit var btnProceedToCard: Button
    private var menuList = arrayListOf<RestaurantMenu>()
    private var cartList = arrayListOf<RestaurantMenu>()
    lateinit var mRef: DatabaseReference

    companion object {
        var restaurantId: Int? = 0
        var restaurantName: String? = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restaurant_menu, container, false)

        /*getting restaurant id from AllRestaurantFragment using Bundle*/
        restaurantId = arguments?.getInt("res_id", 0)
        restaurantName = arguments?.getString("res_name")

        setHasOptionsMenu(true)

        /* finding views by id*/
        resDetailsRecyclerView = view.findViewById(R.id.resDetailsRecyclerView)
        rlLoading = view.findViewById(R.id.rlLoading)
        btnProceedToCard = view.findViewById(R.id.btnProceedToCart)

        /*Setting title to toolbar, Enabling home button and lock the drawer*/
        (activity as AppCompatActivity).supportActionBar?.title = restaurantName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        /* Disable Hamburger icon in fragment */
        disableToggle()

        /*Getting data from database*/
        rlLoading.visibility = View.VISIBLE
        gettingDataFromDB()

        /**Adding ordered food in Database using Gson class
         * Gson is nothing but its only converting list to a String type so that we can store a COMPLEX data in DATABASE*/
        proceedToCart()
        return view
    }

    private fun gettingDataFromDB() {

        mRef = when {
            restaurantId!! <= 7 -> {
                FirebaseDatabase.getInstance().getReference("ResMenu").child("Breakfast")
                    .child(restaurantId.toString())
            }
            restaurantId!! in 8..15 -> {
                FirebaseDatabase.getInstance().getReference("ResMenu").child("Lunch")
                    .child(restaurantId.toString())
            }
            else -> {
                FirebaseDatabase.getInstance().getReference("ResMenu").child("Dinner")
                    .child(restaurantId.toString())
            }
        }

        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            mRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        menuList.clear()
                        for (ss in snapshot.children) {
                            val value = ss.getValue(RestaurantMenu::class.java)
                            menuList.add(value!!)

                            resMenuRecyclerAdapter =
                                ResMenuRecyclerAdapter(
                                    activity as Context,
                                    menuList,
                                    object : ResMenuRecyclerAdapter.OnItemClickListener {
                                        override fun onAddItemClick(
                                            orderItems: RestaurantMenu
                                        ) {
                                            /*hiding  btnProceedToCard visible */
                                            btnProceedToCard.visibility = View.VISIBLE
                                            cartList.add(orderItems) /*Adding food to cart list*/
                                            ResMenuRecyclerAdapter.isCartEmpty = false
                                        }

                                        override fun onRemoveItemClick(orderItems: RestaurantMenu) {
                                            cartList.remove(orderItems) /*Removing food from cart list*/
                                            if (cartList.isEmpty()) {
                                                /*hiding  btnProceedToCard */
                                                btnProceedToCard.visibility = View.GONE
                                                ResMenuRecyclerAdapter.isCartEmpty = true
                                            }
                                        }

                                    })

                            setUpRecyclerView()
                            rlLoading.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    rlLoading.visibility = View.INVISIBLE
                    Toast.makeText(activity as Context, "$error", Toast.LENGTH_SHORT).show()
                }

            })
        } else {
            rlLoading.visibility = View.INVISIBLE
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun disableToggle() {
        toggle = ActionBarDrawerToggle(
            activity,
            (activity as AppCompatActivity).drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        /*replace hamburger icon to arrow icon*/
        toggle.isDrawerIndicatorEnabled = false
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun proceedToCart() {
        btnProceedToCard.setOnClickListener {
            val gson = Gson()
            val foodItems = gson.toJson(cartList)

            val addItemsToCart =
                DBCartAsyncTask(activity as Context, restaurantId!!, foodItems, 1).execute()
            val success = addItemsToCart.get()
            if (success) {
                val data = Intent(
                    activity as Context,
                    _root_ide_package_.com.knesarCreation.foodmunch.activity.CartActivity::class.java
                )
                data.putExtra("res_id", restaurantId)
                data.putExtra("res_name", restaurantName)
                startActivity(data)
            } else {
                Toast.makeText(
                    activity as Context, "Some unexpected error occurred", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun setUpRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(activity as Context)

        /*setting layout manager*/
        resDetailsRecyclerView.layoutManager = linearLayoutManager

        /*setting adapter*/
        resDetailsRecyclerView.adapter = resMenuRecyclerAdapter
    }

    private fun openAllRestaurantsFragment() {
        activity!!.supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            AllRestaurantsFragment()
        ).commit()

        (activity as AppCompatActivity).supportActionBar?.title = "All Restaurants"
        (activity as AppCompatActivity).navigationView.setCheckedItem(R.id.allRestaurants)
        (activity as AppCompatActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        /*Enable the hamburger icon*/
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            when (activity!!.supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                !is AllRestaurantsFragment -> {
                    /*checking that cartList is empty or not*/
                    if (!ResMenuRecyclerAdapter.isCartEmpty) {
                        /* if cartLis is not empty then conformation dialog will appear*/
                        val dialog = AlertDialog.Builder(activity as Context)
                        dialog.setTitle("Confirmation")
                        dialog.setMessage("Going back will reset cart items. Do you still want to proceed")
                        dialog.setPositiveButton("OK") { _, _ ->

                            /*Deleting cart items*/
                            DBCartAsyncTask(
                                activity as Context,
                                restaurantId!!,
                                "null",
                                2
                            ).execute()
                            openAllRestaurantsFragment()
                            ResMenuRecyclerAdapter.isCartEmpty = true
                        }
                        dialog.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        dialog.show()
                    } else {
                        /*if cartList is empty then it simply open  AllRestaurantsFragment*/
                        openAllRestaurantsFragment()
                    }
                }
            }
        }
        return true
    }

    class DBCartAsyncTask(
        context: Context,
        private val resId: Int,
        private val foodItems: String,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val cartEntity = CartEntity(resId, foodItems)
                    db.cartDao().insertOrder(cartEntity)
                    db.close()
                    return true
                }
                2 -> {
                    db.cartDao().deleteOrder(resId)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}