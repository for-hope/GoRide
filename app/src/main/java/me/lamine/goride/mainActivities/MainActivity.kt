package me.lamine.goride.mainActivities

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import me.lamine.goride.postingActivity.PostOptionsActivity
import me.lamine.goride.R
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.notificationActivity.InboxActivity
import me.lamine.goride.reviewActivity.ReviewActivity
import me.lamine.goride.searchActivity.SearchTripActivity
import me.lamine.goride.signActivity.LoginActivity


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var user:FirebaseUser? = null
    private var menu:Menu?=null
    private lateinit var database:DatabaseReference
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        //init

        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.playTabLayout1)
        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        drawerLayout = findViewById(R.id.drawer_layout)
        nav_view.setNavigationItemSelectedListener(this)
         database = FirebaseDatabase.getInstance().reference
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            for (profile in user!!.providerData) {
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
            Log.i("FAILED", " FAILED LOG")
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("tab",1)
            startActivity(intent)
            finish()


        }
        if (user!= null){
            getUser()
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, PostOptionsActivity::class.java)
            startActivity(intent)
        }

        val fabP: FloatingActionButton = findViewById(R.id.fab_p)
        fabP.setOnClickListener {
            val intent = Intent(this, SearchTripActivity::class.java)
            startActivity(intent)
        }
        fabP.hide()




        playTabLayout.colors = intArrayOf(
            R.color.colorAccent,
            R.color.colorSecondary
        )


        viewpager_v.adapter = fragmentAdapter
        val tabLayout = playTabLayout.tabLayout
        tabLayout.setupWithViewPager(viewpager_v)
        tabLayout.setTabTextColors(ContextCompat.getColor(this, R.color.colorPrimary),ContextCompat.getColor(this,
            R.color.whiteColor
        ))
        //tabs_main.setupWithViewPager(viewpager_main)
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        toolbar.changeToolbarFont()
        checkNotifications()
       initNavStripe(navigationTabStrip)




        tabLayout.addOnTabSelectedListener(object :
            TouchableTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TouchableTabLayout.Tab) {
                if (tab.position == 0){
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    ))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    )
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    )

                    fabP.hide()
                    fab.show()

                }
                if (tab.position == 1) {
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    ))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    )
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    )

                    fab.hide()
                    fabP.show()
                }
            }
            override fun onTabReselected(tab: TouchableTabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TouchableTabLayout.Tab) {

            }


        })

        askPermission{
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
                    .setPositiveButton("yes") { _, _ ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if(e.hasForeverDenied()) {
                Toast.makeText(this,"Permissions Denied Forever!",Toast.LENGTH_SHORT).show()
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                    Log.i("DENIED FOREVER", it)
                }
                // you need to open setting manually if you really need it
                e.goToSettings()
            }
        }





    }

    override fun onRestart() {
        checkNotifications()
        super.onRestart()

    }

    override fun onResume() {
        checkNotifications()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        checkNotifications()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu
        menuInflater.inflate(R.menu.menu_notif, menu)
        menuInflater.inflate(R.menu.menu_inbox, menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_messages -> {
                val intent = Intent(this, InboxActivity::class.java)
                intent.putExtra("tab",1)
                startActivity(intent)
                return true
            }
            R.id.action_notifications -> {
                val intent = Intent(this, InboxActivity::class.java)
                intent.putExtra("tab",0)
                startActivity(intent)
            }
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_profile -> {
                Toast.makeText(this, "Clicked Profile",Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "CLicked Profile")

            }//do sometehing
            R.id.nav_logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish() }
                //FirebaseAuth.getInstance().signOut()

            }
            R.id.nav_settings -> {
                startActivity(Intent(this@MainActivity, ReviewActivity::class.java))
            }
        }
        //close navigation drawer

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    ///
    private fun checkNotifications(){
        mCheckTripInfoInServer()
    }
    private fun readTripRequests(listener: OnGetDataListener) {
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef= rootRef.child("users").child(user?.uid!!).child("tripRequests")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun mCheckTripInfoInServer() {
        readTripRequests(object : OnGetDataListener {
            override fun onStart() {
                    Log.i("MainActivity", "Looking for notificatons...")
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (menu!=null){
                if (data.childrenCount.toInt() >0 ){
                    menu?.getItem(0)!!.icon = ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_notifications_active_orange_24dp)
                } else {
                    menu?.getItem(0)!!.icon = ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_notifications_none_black_24dp)
                }
                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity,"Error occurred refreshing the page.",Toast.LENGTH_SHORT).show()
            }
        })

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
    private fun saveSharedUser(user:me.lamine.goride.dataObjects.User){
        val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val prefsEditor = mPrefs.edit()
        //val nbTrips = mPrefs.getInt("savedTrips",0)
        val gson = Gson()
        val json = gson.toJson(user)
        val currentUserStr = "currentUser"
        prefsEditor.putString(currentUserStr, json)
        prefsEditor.apply()
    }
    private fun getUser(){

        getUserInfo(object : OnGetDataListener {
            override fun onStart() {
                //setPb(1)
            }
            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

               // var profileUrl = data.child("profilePic").value as String?
                //todo save user object

                val user  = data.getValue(me.lamine.goride.dataObjects.User::class.java)
                saveSharedUser(user!!)

                if (user == null) {
                    AuthUI.getInstance()
                        .signOut(this@MainActivity)
                        .addOnCompleteListener {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                } else {
                    var profileUrl = user?.profilePic
                    val fullName = user?.fullName
                    if(profileUrl == null || profileUrl == ""){
                        profileUrl = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
                    }
                    setHeaderImage(profileUrl,fullName!!)
                }


            }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }
    private fun getUserInfo(listener: OnGetDataListener){
        listener.onStart()

        val newRef = database.child("users").child(user?.uid!!)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors
            }
        }
        newRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun setHeaderImage(link:String,name:String){
        val hView = nav_view.inflateHeaderView(R.layout.nav_header)
//
        val mTextView = hView.findViewById<TextView>(R.id.nav_text)
        mTextView.text =name
        val mImageView = hView.findViewById(R.id.nav_header_pfp) as ImageView
        //val link = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
        Picasso.get().load(link).into(mImageView)
    }
    private fun initNavStripe(navigationTabStrip: NavigationTabStrip) {
        navigationTabStrip.setTitles("Driver", "Passenger")
        navigationTabStrip.setViewPager(viewpager_v,0)
        navigationTabStrip.setTabIndex(0, true)
        navigationTabStrip.setBackgroundColor(Color.WHITE)
        navigationTabStrip.stripColor = Color.RED
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        //todo navigationTabStrip.setTypeface("fonts/typeface.ttf")
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.RED

    }



}
