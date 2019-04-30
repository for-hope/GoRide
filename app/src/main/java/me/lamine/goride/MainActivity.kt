package me.lamine.goride

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.gigamole.navigationtabstrip.NavigationTabStrip
import io.armcha.playtablayout.core.TouchableTabLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    override fun onStart() {
        super.onStart()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //init

        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.playTabLayout1)
        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        drawerLayout = findViewById(R.id.drawer_layout)
        //todo set up 
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")
        //var user:FirebaseUser = intent.getParcelableExtra("User")
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            for (profile in user.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid

                // Name, email address, and profile photo Url
                val name = profile.displayName
                val email = profile.email
                Log.i("LOGINTEST", "Welcome $providerId, $name ,$uid, $email")
                Toast.makeText(this,"Welcome back $name", Toast.LENGTH_SHORT).show()
               // val photoUrl = profile.photoUrl
            }
        } else {
            Log.i("FAILED", " FAiLED LOG")
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Here's a Snackbar For Driver", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val fabp: FloatingActionButton = findViewById(R.id.fab_p)
        fabp.setOnClickListener { view ->
            Snackbar.make(view, "Here's a Passenger", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        fabp.hide()




        playTabLayout.colors = intArrayOf(
            R.color.colorAccent,
            R.color.colorSecondary)


        viewpager_v.adapter = fragmentAdapter
        val tabLayout = playTabLayout.tabLayout
        tabLayout.setupWithViewPager(viewpager_v)
        tabLayout.setTabTextColors(ContextCompat.getColor(this,R.color.colorPrimary),ContextCompat.getColor(this,R.color.whiteColor))
        //tabs_main.setupWithViewPager(viewpager_main)
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        toolbar.changeToolbarFont()

       initNavStripe(navigationTabStrip)




        tabLayout.addOnTabSelectedListener(object :
            TouchableTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TouchableTabLayout.Tab) {
                if (tab.position == 0){
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,R.color.colorAccent))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,R.color.colorAccent)
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,R.color.colorAccent)

                    fab_p.hide()
                    fab.show()

                }
                if (tab.position == 1) {
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,R.color.colorSecondary))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,R.color.colorSecondary)
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,R.color.colorSecondary)

                    fab.hide()
                    fab_p.show()
                }
            }
            override fun onTabReselected(tab: TouchableTabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TouchableTabLayout.Tab) {

            }


        })

        askPermission(){
            //all permissions already granted or just granted

            Toast.makeText(this,"Permissions Granted!",Toast.LENGTH_SHORT).show()
        }.onDeclined { e ->
            if (e.hasDenied()) {
                Toast.makeText(this,"Permissions Denied!",Toast.LENGTH_SHORT).show()
                //the list of denied permissions
                e.denied.forEach {
                   Log.i("DENIED FOR EACH", it)
                }

                AlertDialog.Builder(this)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if(e.hasForeverDenied()) {
                Toast.makeText(this,"Permissions Denied Forever!",Toast.LENGTH_SHORT).show()
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                    Log.i("DENIED FOREVER", it)
                }
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }





    }
    private fun Toolbar.changeToolbarFont(){
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is TextView && view.text == title) {
                view.typeface = ResourcesCompat.getFont(context, R.font.poppins_bold)
                break
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_add, menu)
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_favorite) {
            Toast.makeText(this@MainActivity, "Action clicked", Toast.LENGTH_LONG).show()
            val intent = Intent(this, SearchTripActivity::class.java)
            startActivity(intent)
            return true
        }
        else if (id == R.id.action_add){
            Toast.makeText(this, "Add acitivty", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PostOptionsActivity::class.java)
            startActivity(intent)
        } else if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }
    private fun initNavStripe(navigationTabStrip: NavigationTabStrip) {
        navigationTabStrip.setTitles("Driver", "Passenger")
        navigationTabStrip.setViewPager(viewpager_v,0)
        navigationTabStrip.setTabIndex(0, true)
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
//todo fix