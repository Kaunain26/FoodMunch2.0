package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.AllRestaurantsRecyclerAdapter
import com.knesarCreation.foodmunch.model.Restaurants

class SearchRestaurantsFragment : Fragment() {

    lateinit var searchView: SearchView
    lateinit var mRecyclerView: RecyclerView
    lateinit var rlLoading: RelativeLayout
    lateinit var mRef: DatabaseReference
    lateinit var mAdapter: AllRestaurantsRecyclerAdapter
    val resList = arrayListOf<Restaurants>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_restaurants, container, false)

        searchView = view.findViewById(R.id.searchRestaurants)
        mRecyclerView = view.findViewById(R.id.searchrecyclerView)
        rlLoading = view.findViewById(R.id.rlLoading)

        /*searching restaurant*/
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })

        mRef = FirebaseDatabase.getInstance().getReference("AllRestaurants")
        /*getting data from Firebase database*/
        gettingDataFromDB()

        return view
    }

    private fun gettingDataFromDB() {

        mRef.child("FoodMunchRestaurants").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    resList.clear()
                    for (ss in snapshot.children) {
                        val value = ss.getValue(Restaurants::class.java)
                        resList.add(value!!)
                    }
                    mRef.child("PopularRestaurants")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (ss in snapshot.children) {
                                        val value1 = ss.getValue(Restaurants::class.java)
                                        resList.add(value1!!)
                                    }
                                    mRef.child("RecommendedRestaurants")
                                        .addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    for (ss in snapshot.children) {
                                                        val value1 =
                                                            ss.getValue(Restaurants::class.java)
                                                        resList.add(value1!!)
                                                        setUpRecyclerView()
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                rlLoading.visibility = View.INVISIBLE
                                                Toast.makeText(
                                                    activity as Context,
                                                    "$error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                rlLoading.visibility = View.INVISIBLE
                                Toast.makeText(
                                    activity as Context,
                                    "$error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                rlLoading.visibility = View.INVISIBLE
                Toast.makeText(activity as Context, "$error", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setUpRecyclerView() {
        mAdapter =
            AllRestaurantsRecyclerAdapter(
                activity as Context,
                resList,
                activity!!.supportFragmentManager,
                1
            )

        rlLoading.visibility = View.INVISIBLE

        /*setting layoutManager  to recycler view*/
        val linearLayoutManager = LinearLayoutManager(activity as Context)
        mRecyclerView.layoutManager = linearLayoutManager

        /*setting adapter to recycler view*/
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }
}