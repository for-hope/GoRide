package me.lamine.goride

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.io.Serializable
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import android.R.id.edit
import android.util.Log


class MyTripResultPageAdapter(fm: FragmentManager,trip:ArrayList<String>) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    private var item = trip!!
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val bundle = Bundle()
             //   bundle.putSerializable("PassedTrip",item)

                bundle.putStringArrayList("TripsList", item)
                val tripResultsFragment:Fragment = TripResultFragment()
                tripResultsFragment.arguments = bundle
                tripResultsFragment

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