package com.knesarCreation.foodmunch.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.knesarCreation.foodmunch.fragments.TabItemsFragment
import com.knesarCreation.foodmunch.model.TabTitle

class PagerAdapter(fm: FragmentManager, private val tabItemList: List<TabTitle>) :
    FragmentPagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    override fun getCount(): Int {
        return tabItemList.size
    }

    override fun getItem(position: Int): Fragment {
        return TabItemsFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabItemList[position].title
    }
}