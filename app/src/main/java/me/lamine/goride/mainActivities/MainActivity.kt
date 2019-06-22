package me.lamine.goride.mainActivities

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
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_inbox.*
import kotlinx.android.synthetic.main.fragment_passenger.*
import me.lamine.goride.postingActivity.PostOptionsActivity
import me.lamine.goride.R
import me.lamine.goride.adminActivities.AdminActivity
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.javaClasses.RevealAnimation
import me.lamine.goride.inboxActivity.InboxActivity
import me.lamine.goride.requestActivity.RequestTripActivity
import me.lamine.goride.searchActivity.SearchTripActivity
import me.lamine.goride.settingsActivity.SettingsActivity
import me.lamine.goride.signActivity.LoginActivity
import me.lamine.goride.userActivity.UserActivity
import me.lamine.goride.utils.*
import java.lang.Exception


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var user:FirebaseUser? = null
    private var menu:Menu?=null
    private lateinit var mUser: User
    private lateinit var mDatabase:Database
    private lateinit var mRevealAnimation: RevealAnimation
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val rootLayout = root_layout
        mRevealAnimation = RevealAnimation(rootLayout, intent, this)
        initPermissions()
        if (!verifyAvailableNetwork(this)){
          Toast.makeText(this,"No Available Network.", Toast.LENGTH_SHORT).show()
        }
        mDatabase = Database()
        mDatabase.checkUserSession(this)
        user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this,"Login First", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            //

        } else {
            getUser()

           // mUser = getSharedUser(this)
        /*   for (profile in user!!.providerData) {
                Log.i("inits", "STARTEd")
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid

                // Name, email address, and profile photo Url
                val name = profile.displayName
                val email = profile.email
                Log.i("LOGIN_TEST", "Welcome $providerId, $name ,$uid, $email")
                if (providerId == "firebase"){
                    Toast.makeText(this,"Welcome back $name", Toast.LENGTH_SHORT).show()
                }

                // val photoUrl = profile.photoUrl
            }*/
            //init




            initTabLayout()

       // initTabLayout()
     //   initPermissions()




        }

    }

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }
    private fun initUserInfo(){
       // val tabLayout = playTabLayout.tabLayout
        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.playTabLayout1)
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        val fabP = findViewById<FloatingActionButton>(R.id.fab_p)
        val rootLayout = root_layout
        if(mUser.superuser == 1){
            my_toolbar.inflateMenu(R.menu.menu_admin)
        }
        if (getSharedUser(this).isDriver){
            initNavStripe(navigationTabStrip,0,toolbar)
            navigationTabStrip.tabIndex = 0
            Log.i("RECHED", "0")
        } else {
            initNavStripe(navigationTabStrip,1,toolbar)
            navigationTabStrip.tabIndex = 1
            Log.i("RECHED", "1")
        }

        var driverSnackbar: Snackbar? = null
        var passSnackbar: Snackbar? = null
        Log.i("RECHED", "THIS CODE")
        //todo

        navigationTabStrip.onTabStripSelectedIndexListener = object :
            NavigationTabStrip.OnTabStripSelectedIndexListener{
            override fun onEndTabSelected(title: String?, index: Int) {
                if (index == 0){
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    ))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    )
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,
                        R.color.colorAccent
                    )
                    //
                    navigationTabStrip.tabIndex = 0
                    if (!getSharedUser(this@MainActivity).isDriver){
                        passSnackbar = Snackbar.make(rootLayout, "You're a passenger! But you can drive as well.", Snackbar.LENGTH_INDEFINITE)
                        passSnackbar!!.setAction("Proceed") {
                            navigationTabStrip.tabIndex = 0

                            fabP.hide()
                            fab.show()

                        }
                        passSnackbar!!.show()


                    } else {
                        driverSnackbar?.dismiss()
                        fabP.hide()
                        fab.show()
                    }

                }
                if (index == 1){
                    navigationTabStrip.tabIndex = 1
                    toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    ))
                    navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    )
                    navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,
                        R.color.colorSecondary
                    )
                    if (getSharedUser(this@MainActivity).isDriver){
                        driverSnackbar =Snackbar.make(rootLayout, "You're a driver only! You can check trips but you can't register in one..", Snackbar.LENGTH_INDEFINITE)
                        driverSnackbar!!.setAction("Proceed") {
                            fab.hide()
                            fabP.show()
                        }
                        driverSnackbar!!.show()


                    } else {
                        passSnackbar?.dismiss()
                        fab.hide()
                        fabP.show()
                    }
                }
            }
            override fun onStartTabSelected(title: String?, index: Int) {}
        }


/*
        tabLayout.addOnTabSelectedListener(object :
            TouchableTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TouchableTabLayout.Tab) {
                if (tab.position == 0){


                }
                if (tab.position == 1) {


                }
            }
            override fun onTabReselected(tab: TouchableTabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TouchableTabLayout.Tab) {

            }


        })*/

    }
    private fun initNavigationTabLayout(){
        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.playTabLayout1)
        val fragmentAdapter =
            MyPagerAdapter(supportFragmentManager)
        viewpager_v.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_v)
        //initNavStripe(navigationTabStrip,0,my_toolbar)
        try {
            val page =  if (getSharedUser(this).isDriver){
                0
            } else {
                1
            }
            initNavStripe(navigationTabStrip,page,my_toolbar)
            navigationTabStrip.tabIndex = page
        } catch (e:Exception){
            e.stackTrace
        }
    }
    private fun initTabLayout(){



       // val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        ///
        //
        drawerLayout = findViewById(R.id.drawer_layout)
        nav_view.setNavigationItemSelectedListener(this)
        //
        //

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
        //
        //
        initNavigationTabLayout()
        /*
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
        */
        //tabs_main.setupWithViewPager(viewpager_main)
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        toolbar.changeToolbarFont()
        checkForNotifications()
     //todo
        //val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.playTabLayout1)
      //  initNavStripe(navigationTabStrip,0,toolbar)


    }
    private fun initPermissions(){
        Log.i("initPermissions", "STARTEd")
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
            R.id.action_admin -> {
                val intent = Intent(this, AdminActivity::class.java)
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
                val intent = Intent(this, UserActivity::class.java)
                val user = getSharedUser(this)
                intent.putExtra("UserProfile",user )
                startActivity(intent)

            }//do sometehing
            R.id.nav_logout -> {
                mDatabase.signOut(this)
                /*saveUserIsLogged(false)
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish() }*/
                //FirebaseAuth.getInstance().signOut()

            }
            R.id.nav_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
        //close navigation drawer

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    ///
    private fun checkForNotifications(){
       // mCheckTripInfoInServer()
        checkNotifications(object :OnGetDataListener{
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
               if (data.hasChildren()){
                   if (menu!=null){
                       if (data.childrenCount.toInt() >0 ){
                           menu?.getItem(0)!!.icon = ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_notifications_active_orange_24dp)
                       } else {
                           menu?.getItem(0)!!.icon = ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_notifications_none_black_24dp)
                       }
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


    private fun getUser(){
       mDatabase.fetchUser(user?.uid!!,object : OnGetDataListener {
            override fun onStart() {

            }
            override fun onSuccess(data: DataSnapshot) {
                val user  = data.getValue(User::class.java)
                if (user == null) {
                    mDatabase.signOut(this@MainActivity)
                } else {
                    saveSharedUser(this@MainActivity,user)
                    mUser = getSharedUser(this@MainActivity)
                    val profileUrl = user.profilePic
                    val fullName = user.fullName
                    initUserInfo()


                    setHeaderImage(profileUrl,fullName)
                }


            }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }

    private fun setHeaderImage(link:String,name:String){
        val hView = nav_view.inflateHeaderView(R.layout.nav_header)
        var mLink = link
        val mTextView = hView.findViewById<TextView>(R.id.nav_text)
        mTextView.text =name
        if (mUser.superuser == 1){
            val mAdminText = hView.findViewById<TextView>(R.id.nav_admin)
            mAdminText.visibility = View.VISIBLE
        }
        val mImageView = hView.findViewById(R.id.nav_header_pfp) as ImageView
        if (link == ""){
              mLink = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
            }
        Log.i("SETTING IMAGE", "DONE")
        Picasso.get().load(mLink).into(mImageView)
    }
    private fun initNavStripe(navigationTabStrip: NavigationTabStrip,tab:Int,toolbar: Toolbar) {
        navigationTabStrip.setTitles("Driver", "Passenger")
        navigationTabStrip.setViewPager(viewpager_v,tab)
        navigationTabStrip.setTabIndex(tab, true)
        navigationTabStrip.setBackgroundColor(Color.WHITE)
        if (tab == 0) {
            navigationTabStrip.stripColor = Color.RED
            navigationTabStrip.activeColor = Color.RED
        } else {
            navigationTabStrip.stripColor = ContextCompat.getColor(applicationContext,
                R.color.colorSecondary
            )
            navigationTabStrip.activeColor = ContextCompat.getColor(applicationContext,
                R.color.colorSecondary
            )
            toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext,
                R.color.colorSecondary
            ))
            fab.hide()
            fab_p.show()
        }
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        //todo navigationTabStrip.setTypeface("fonts/typeface.ttf")
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY


    }



}
