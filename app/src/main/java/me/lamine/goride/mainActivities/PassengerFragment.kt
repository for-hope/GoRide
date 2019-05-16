package me.lamine.goride.mainActivities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import me.lamine.goride.R


/**
 * A simple [Fragment] subclass.
 */
class PassengerFragment : androidx.fragment.app.Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<TextView>(R.id.click_here_p_textview_)
            ?.setOnClickListener { Toast.makeText(this.context,"Post a request",Toast.LENGTH_SHORT).show() }

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_passenger, container, false)

    }
}