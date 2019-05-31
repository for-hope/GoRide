package me.lamine.goride.userActivity


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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Review
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.notificationActivity.MessageActivity
import me.lamine.goride.reviewActivity.ReviewActivity
import me.lamine.goride.utils.Database
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {

    private lateinit var mUser: User
    private var listOfReviews: MutableList<Review> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(user_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mUser = intent.getSerializableExtra("UserProfile") as User

        //setup reviews list
        this.review_list.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        review_list.isNestedScrollingEnabled = false
        review_list.layoutManager = llm
        Log.i("size", mUser.userReviews.size.toString())
        getAllReviews(mUser.userReviews)

        //setup profile picture
        Picasso.get().load(mUser.profilePic).into(user_imageview)
        //setup name
        user_ac_username.text = mUser.fullName
        //gender and age
        val mAgeFormat = "dd, MMM yyyy"
        user_toolbar.title = mUser.fullName
        val userBirthday = mUser.birthday
        val df = SimpleDateFormat(mAgeFormat, Locale.US)
        val uBirthday = df.parse(userBirthday)
        val cal = Calendar.getInstance()
        cal.time = uBirthday
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        val age = getAge(year, month, day)
        var gender = mUser.gender.capitalize()
        gender = if (gender == "F") {
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
        if (!mUser.emailVerification) {
            user_ac_email_verify.text = getString(R.string.unverified)
            user_ac_email_verify.textColor = Color.GRAY
        }
        if (!mUser.phoneVerfication) {
            user_ac_phone_verify.text = getString(R.string.unverified)
            user_ac_phone_verify.textColor = Color.GRAY
        }
        val reviewsCount = "Reviews(${mUser.userReviews.size})"
        user_ac_review_count.text = reviewsCount

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        if (checkForReview()) {
            inflater.inflate(R.menu.menu_review, menu)
        }
        if (mUser.userId != Database().currentUserId()){
            inflater.inflate(R.menu.menu_message, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_review -> {
                val intent = Intent(this, ReviewActivity::class.java)
                intent.putExtra("userID", mUser.userId)
                startActivity(intent)
                true  }
            R.id.action_message -> {
                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("userId", mUser.userId)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getAllReviews(userReviews: HashMap<String, Any>) {
        Log.i("REVIEWS", userReviews.toString())
        var mReview: Review
        for (review in userReviews) {
            mReview = Review(review.key)
            val reviewHash: HashMap<*, *> = review.value as HashMap<*, *>
            mReview.reviewDesc = reviewHash["review"].toString()
            mReview.reviewDate = reviewHash["timestamp"].toString()
            listOfReviews.add(mReview)
        }
        if (listOfReviews.size > 0) {
            getUsers()
        }

    }

    private fun getUsers() {
        for (review in listOfReviews) {
            Database().fetchUser(review.userId, object : OnGetDataListener {
                override fun onStart() {
                    Toast.makeText(this@UserActivity, "Loading", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(data: DataSnapshot) {
                    val mUser = data.getValue(User::class.java)
                    review.reviewUser = mUser
                    if (review == listOfReviews.last()) {
                        Toast.makeText(this@UserActivity, "DONE", Toast.LENGTH_SHORT).show()
                        review_list.adapter = ReviewsAdapter(listOfReviews)
                    }
                }

                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(this@UserActivity, "Error fetching reviews", Toast.LENGTH_SHORT).show()
                }


            })
        }


    }

    private fun checkForReview(): Boolean {
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

    private fun getSharedUser(): User {
        val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json = mPrefs.getString("currentUser", "")
        //-- return obj
        return gson.fromJson(json, User::class.java)
    }
}