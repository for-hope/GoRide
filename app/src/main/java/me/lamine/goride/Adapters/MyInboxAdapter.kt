package me.lamine.goride.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.lamine.goride.DriverFragment
import me.lamine.goride.NotificationFragment
import me.lamine.goride.PassengerFragment

class MyInboxAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                NotificationFragment()
            }
            else -> {
                return  NotificationFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "NOTIFICATIONS"
            else -> {
                return "MESSAGES"
            }
        }
    }

}