package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.AllRestaurantsRecyclerAdapter
import com.knesarCreation.foodmunch.model.Restaurants
import com.knesarCreation.foodmunch.util.ConnectionManager

class TabItemsFragment : Fragment() {
    lateinit var recyclerRestaurants: RecyclerView
    lateinit var rlLoading: RelativeLayout
    lateinit var mRef: DatabaseReference
    lateinit var mAdapter: AllRestaurantsRecyclerAdapter
    var tabPos: Int = 0
    val resList = arrayListOf<Restaurants>()

    companion object {
        const val KEY_CATEGORY = "category"
        fun newInstance(position: Int): TabItemsFragment {
            return TabItemsFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_CATEGORY, position)
                    tabPos = position
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tab_items, container, false)

        recyclerRestaurants = view.findViewById(R.id.restaurantsRecyclerView)
        rlLoading = view.findViewById(R.id.rlLoading)

        /*getting data from Firebase database*/
        gettingDataFromDB()

        return view
    }

    private fun gettingDataFromDB() {

        mRef = when (tabPos) {
            0 -> {
                FirebaseDatabase.getInstance().getReference("AllRestaurants")
                    .child("FoodMunchRestaurants")
            }
            1 -> {
                FirebaseDatabase.getInstance().getReference("AllRestaurants")
                    .child("PopularRestaurants")

            }
            else -> {
                FirebaseDatabase.getInstance().getReference("AllRestaurants")
                    .child("RecommendedRestaurants")
            }
        }

        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            mRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        resList.clear()
                        for (ss in snapshot.children) {
                            val value = ss.getValue(Restaurants::class.java)
                            resList.add(value!!)

                            try {
                                mAdapter =
                                    AllRestaurantsRecyclerAdapter(
                                        activity as Context,
                                        resList,
                                        activity!!.supportFragmentManager,
                                        1
                                    )
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }

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
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpRecyclerView() {

        /*setting layoutManager  to recycler view*/
        val linearLayoutManager = LinearLayoutManager(activity as Context)
        recyclerRestaurants.layoutManager = linearLayoutManager

        /*setting adapter to recycler view*/
        recyclerRestaurants.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

}