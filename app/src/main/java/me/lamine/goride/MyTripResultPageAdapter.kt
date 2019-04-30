package me.lamine.goride

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


class MyTripResultPageAdapter(fm: FragmentManager,trip:ArrayList<String>,toText:String,fromText:String) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    private var item = trip!!
    private var toText = toText
    private var fromText = fromText
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val bundle = Bundle()
             //   bundle.putSerializable("PassedTrip",item)
              //  bundle.putSerializable()
                bundle.putStringArrayList("TripsList", item)
                bundle.putString("toText",toText)
                bundle.putString("fromText",fromText)
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