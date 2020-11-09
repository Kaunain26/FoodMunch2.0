package com.knesarCreation.foodmunch.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.PagerAdapter
import com.knesarCreation.foodmunch.model.TabTitle

class AllRestaurantsFragment : Fragment() {

    lateinit var mTabLayout: TabLayout
    lateinit var mViewPager: ViewPager

    //    private val restaurantsList = arrayListOf<Restaurants>()
    lateinit var searchView: SearchView
    private var checkedItem = -1
    lateinit var pagerAdapter: PagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_restaurants, container, false)

        /*finding views by id*/
//        searchView = view.findViewById(R.id.dashboardSearchView)
        mTabLayout = view.findViewById(R.id.mTabLayout)
        mViewPager = view.findViewById(R.id.mViewPager)

        setHasOptionsMenu(true)
        /*setting TabLayout with ViewPager*/
        settingUpTabItemsWithViewPager()

        return view
    }


    private fun settingUpTabItemsWithViewPager() {
        mTabLayout.addTab(mTabLayout.newTab().setText("Breakfast"))
        mTabLayout.addTab(mTabLayout.newTab().setText("Lunch"))
        mTabLayout.addTab(mTabLayout.newTab().setText("Dinner"))

        mTabLayout.setupWithViewPager(mViewPager)

        /*setting adapter to pager adapter*/
        pagerAdapter = PagerAdapter(this.childFragmentManager, tabTitles())
        mViewPager.adapter = pagerAdapter
        pagerAdapter.notifyDataSetChanged()

        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))
    }

    private fun tabTitles(): List<TabTitle> {
        return listOf(
            TabTitle("Breakfast"),
            TabTitle("Lunch"),
            TabTitle("Dinner")
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}