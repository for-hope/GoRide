package me.lamine.goride

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MyPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> {
                FirstFragment()
            }
            else -> {
                return SecondFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Driver"
            else -> {
                return "Passenger"
            }
        }
    }

}