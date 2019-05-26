package me.lamine.goride.userActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Review
import me.lamine.goride.dataObjects.User
import me.lamine.goride.dataObjects.UserReview
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.reviewActivity.ReviewActivity
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
private lateinit var database: DatabaseReference
private var currentUser: FirebaseUser? = null
private var mAuth: FirebaseAuth? = null
class UserActivity:AppCompatActivity() {

    private lateinit var mUser:User
    private var listOfReviews:MutableList<Review> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(user_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mUser = intent.getSerializableExtra("UserProfile") as User

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(this,"LOGIN FIRST", Toast.LENGTH_LONG).show()
            val activity = this
            activity.finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        database = FirebaseDatabase.getInstance().reference


        //setup reviews list
        this.review_list.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        review_list.isNestedScrollingEnabled = false
        review_list.layoutManager = llm
        Log.i("size",mUser.userReviews.size.toString())
        getAllReviews(mUser.userReviews)

        //setup profile picture
        Picasso.get().load(mUser.profilePic).into(user_imageview)
        //setup name
        user_ac_username.text = mUser.fullName
        //gender and age
        val age_format = "dd, MMM yyyy"
        user_toolbar.title = mUser.fullName
        val userBirthday = mUser.birthday
        val df = SimpleDateFormat(age_format, Locale.US)
        val uBirthdate = df.parse(userBirthday)
        val cal = Calendar.getInstance()
        cal.time = uBirthdate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        val age = getAge(year, month, day)
        var gender = mUser.gender.capitalize()
        gender = if (gender=="F"){
            "Female"
        } else {
            "Male"
        }
        val genderAndAge = "$gender, $age"
        user_ac_genderAge.text = genderAndAge
        Log.i("UserActivity", mUser.userRating.toString())
        user_ac_ratings.text = mUser.userRating.toString()
        val reviewsText = "${mUser.userReviews.size} Reviews"
        user_ac_ratings2.text = reviewsText
        user_ac_nb_people.text = mUser.peopleDriven.toString()
        user_ac_nb_trips.text = mUser.tripsTraveled.toString()
        user_ac_desc.text = mUser.description
        user_ac_joined.text = mUser.accountCreatingDate
        if (!mUser.emailVerification){
            user_ac_email_verify.text = getString(R.string.unverified)
            user_ac_email_verify.textColor = Color.GRAY
        }
        if (!mUser.phoneVerfication){
            user_ac_phone_verify.text = getString(R.string.unverified)
            user_ac_phone_verify.textColor = Color.GRAY
        }
        val reviewsCount = "Reviews(${mUser.userReviews.size.toString()})"
        user_ac_review_count.text = reviewsCount

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (checkForReview()){
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_review, menu)
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_review -> {
                val intent = Intent(this,ReviewActivity::class.java)
                intent.putExtra("userID",mUser.userId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllReviews(userReviews:HashMap<String,Any>){
        Log.i("REVIEWS",userReviews.toString())
        var mReview:Review
        for (review in userReviews){
            mReview = Review(review.key)
            val reviewHash: HashMap<*, *> = review.value as HashMap<*, *>
            mReview.reviewDesc = reviewHash["review"].toString()
            mReview.reviewDate = reviewHash["timestamp"].toString()
            listOfReviews.add(mReview)
        }
        if (listOfReviews.size >0){
            getUsers()
        }

    }
    private fun fetchReviwersFromDB(userId:String,listener:OnGetDataListener){
        listener.onStart()
        val userRef = database.child("users").child(userId)
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
        userRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun getUsers(){
        for (review in listOfReviews){
            fetchReviwersFromDB(review.userId,object : OnGetDataListener{
                override fun onStart() {
                    Toast.makeText(this@UserActivity,"Loading",Toast.LENGTH_SHORT).show()
                }
                override fun onSuccess(data: DataSnapshot) {
                   val mUser = data.getValue(User::class.java)
                    review.reviewUser = mUser
                    if (review == listOfReviews.last()){
                        Toast.makeText(this@UserActivity,"DONE",Toast.LENGTH_SHORT).show()
                        review_list.adapter = ReviewsAdapter(this@UserActivity,listOfReviews)
                    }
                }
                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(this@UserActivity,"Error fetching reviews",Toast.LENGTH_SHORT).show()
                }


            })
        }


    }
    private fun checkForReview():Boolean{
        return getSharedUser().driversGoneWith.containsKey(mUser.userId)
    }
    private fun getAge(year: Int, month: Int, day: Int): String {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.set(year, month, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        val ageInt = age

        return ageInt.toString()
    }
    private fun getSharedUser():User{
        val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json = mPrefs.getString("currentUser", "")
        val obj = gson.fromJson<User>(json, User::class.java)
        return obj
    }
}