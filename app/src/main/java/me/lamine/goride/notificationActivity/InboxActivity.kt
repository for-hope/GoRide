package me.lamine.goride.notificationActivity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gigamole.navigationtabstrip.NavigationTabStrip
import kotlinx.android.synthetic.main.activity_inbox.*
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
        Log.i(tag,tab.toString())
        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.inbox_tablayout)
        val fragmentAdapter =
            MyInboxAdapter(supportFragmentManager)
        viewpager_inbox.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_inbox)
        initNavStripe(navigationTabStrip,tab)
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