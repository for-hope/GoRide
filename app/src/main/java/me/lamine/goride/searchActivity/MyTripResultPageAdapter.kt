package me.lamine.goride.searchActivity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.lamine.goride.mainActivities.PassengerFragment
import me.lamine.goride.dataObjects.TripSearchData


class MyTripResultPageAdapter(fm: FragmentManager, private var tsd: TripSearchData, toText:String, fromText:String) : androidx.fragment.app.FragmentPagerAdapter(fm) {
   // private var item = trip!!
    private var toText = toText
    private var fromText = fromText
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val bundle = Bundle()
             //   bundle.putSerializable("PassedTrip",item)
              //  bundle.putSerializable()
              //todo  bundle.putStringArrayList("TripsList", item)
                bundle.putString("toText",toText)
                bundle.putString("fromText",fromText)
                bundle.putSerializable("tsd",tsd)
                val tripResultsFragment:Fragment = TripResultFragment()
                tripResultsFragment.arguments = bundle
                tripResultsFragment

            }
            else -> {
                return PassengerFragment()
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