package me.lamine.goride.searchActivity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import me.lamine.goride.dataObjects.TripSearchData


class MyTripResultPageAdapter(
    fm: FragmentManager,
    private var tsd: TripSearchData,
    private var toText: String,
    private var fromText: String
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val bundle = Bundle()
                bundle.putString("toText", toText)
                bundle.putString("fromText", fromText)
                bundle.putSerializable("tsd", tsd)
                val tripResultsFragment: Fragment = TripResultFragment()
                tripResultsFragment.arguments = bundle
                tripResultsFragment

            }
            else -> {
                val bundle = Bundle()
                bundle.putString("toText", toText)
                bundle.putString("fromText", fromText)
                bundle.putSerializable("tsd", tsd)
                val requestsResultsFragment: Fragment = RequestsResultFragment()
                requestsResultsFragment.arguments = bundle
                return requestsResultsFragment
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