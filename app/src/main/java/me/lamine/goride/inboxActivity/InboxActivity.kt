package me.lamine.goride.inboxActivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gigamole.navigationtabstrip.NavigationTabStrip
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.R


class InboxActivity: AppCompatActivity() {
    private val tag = "InboxActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)
        setSupportActionBar(findViewById(R.id.inbox_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val tab = intent.getIntExtra("tab",0)

        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.inbox_tablayout)
        val fragmentAdapter =
            MyInboxAdapter(supportFragmentManager)
        viewpager_inbox.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_inbox)

        initNavStripe(navigationTabStrip,tab)
        fab_clear_notif.setOnClickListener {
            if (snotif_list_res_view.adapter != null){
            val adapter = snotif_list_res_view.adapter as ExtraNotifAdapter
            Toast.makeText(this,"Notifications cleared.", Toast.LENGTH_SHORT).show()
            adapter.removeAll()
            } else {
                Toast.makeText(this,"No Notifications.", Toast.LENGTH_SHORT).show()
            }
        }

      navigationTabStrip.onTabStripSelectedIndexListener = object :
          NavigationTabStrip.OnTabStripSelectedIndexListener{
          override fun onEndTabSelected(title: String?, index: Int) {

          }

          override fun onStartTabSelected(title: String?, index: Int) {
            if (index == 0){
                fab_clear_notif.show()
            }
            if (index == 1){
                fab_clear_notif.hide()
              }
          }

      }
        if (navigationTabStrip.tabIndex == 1){
            fab_clear_notif.hide()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun initNavStripe(navigationTabStrip: NavigationTabStrip,tab:Int) {
        navigationTabStrip.setTitles("NOTIFICATIONS", "MESSAGES")
        navigationTabStrip.setViewPager(viewpager_inbox, tab)
        navigationTabStrip.setTabIndex(tab, true)
        navigationTabStrip.setBackgroundColor(Color.WHITE)
        navigationTabStrip.stripColor = Color.RED
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.setTypeface("fonts/typeface.ttf")
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.RED
    }
}