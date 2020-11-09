package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.OrderHistoryParentAdapter
import com.knesarCreation.foodmunch.model.OrderHistory
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import kotlinx.android.synthetic.main.activity_dashboard.*

class OrderHistoryFragment : Fragment() {

    lateinit var orderHistParentRecyclerView: RecyclerView
    lateinit var orderHistoryParentAdapter: OrderHistoryParentAdapter
    val orderHistoryList = arrayListOf<OrderHistory>()
    lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var rlLoading: RelativeLayout
    lateinit var rlNoOrderHistory: RelativeLayout
    lateinit var mRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_order_history, container, false)

        setHasOptionsMenu(true)

        sessionManager = SessionManager(activity as Context)

        /*initializing views*/
        initViews(view)

        /*setting recycler view*/
        buildRecyclerView(view)
        return view
    }

    private fun initViews(view: View) {
        rlLoading = view.findViewById(R.id.rlLoading)
        rlNoOrderHistory = view.findViewById(R.id.rlNoOrderHistory)
        sharedPreferences = sessionManager.getSharedPreferences
    }

    private fun buildRecyclerView(view: View) {
        orderHistParentRecyclerView = view.findViewById(R.id.orderHistoryParentRecycler)

        rlLoading.visibility = View.VISIBLE  /*progress bar visibility visible*/
        gettingDataFromDB()
    }

    private fun gettingDataFromDB() {
        val userId = sharedPreferences.getString("mobile_number", null)
        /*checking internet Connection*/
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            mRef = FirebaseDatabase.getInstance().getReference("OrderHistory").child(userId!!)
            mRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        orderHistoryList.clear()
                        for (ss in snapshot.children) {
                            val value = ss.getValue(OrderHistory::class.java)

                            orderHistoryList.add(
                                OrderHistory(
                                    value?.orderId!!,
                                    value.resName,
                                    value.totalCost,
                                    value.orderDate,
                                    value.foodItem
                                )
                            )

                            try {
                                orderHistoryParentAdapter =
                                    OrderHistoryParentAdapter(activity!!, orderHistoryList)
                                orderHistParentRecyclerView.adapter = orderHistoryParentAdapter
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } finally {
                                orderHistoryList.reverse()
                                rlLoading.visibility = View.GONE
                            }


                        }
                    } else {
                        if (orderHistoryList.isEmpty()) {
                            rlNoOrderHistory.visibility = View.VISIBLE
                        }
                        rlLoading.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    rlLoading.visibility = View.GONE
                    Toast.makeText(activity, "$error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            (activity as AppCompatActivity).drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}