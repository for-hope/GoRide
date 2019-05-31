package me.lamine.goride.reviewActivity


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_review.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.User
import me.lamine.goride.dataObjects.UserReview
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database
import org.jetbrains.anko.image
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ReviewActivity:AppCompatActivity() {
    private lateinit var arrayOfStars:ArrayList<ImageView>
    private lateinit var arrayOfTStars:ArrayList<ImageView>
    private lateinit var arrayOfSStars:ArrayList<ImageView>
    private lateinit var arrayOfCStars:ArrayList<ImageView>
    private var userID = ""
    private lateinit var mDatabase : Database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        setSupportActionBar(review_toolbar)
        userID = intent.getStringExtra("userID")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_blackest_24dp)
        mDatabase = Database()

        //
        arrayOfStars = arrayListOf(review_star_1,review_star_2,review_star_3,review_star_4,review_star_5)
        setupRatingStars(arrayOfStars)
        arrayOfTStars = arrayListOf(review_t_star_1,review_t_star_2,review_t_star_3,review_t_star_4,review_t_star_5)
        setupRatingStars(arrayOfTStars)
        review_clear_t.setOnClickListener { clearRatingStars(arrayOfTStars) }
        arrayOfSStars = arrayListOf(review_s_star_1,review_s_star_2,review_s_star_3,review_s_star_4,review_s_star_5)
        setupRatingStars(arrayOfSStars)
        review_clear_s.setOnClickListener { clearRatingStars(arrayOfSStars) }
        arrayOfCStars = arrayListOf(review_c_star_1,review_c_star_2,review_c_star_3,review_c_star_4,review_c_star_5)
        setupRatingStars(arrayOfCStars)
        review_clear_c.setOnClickListener { clearRatingStars(arrayOfCStars) }

    }
    private fun fetchUser(userReview: UserReview){
       Database().fetchUser(userID,object :OnGetDataListener{
           override fun onStart() {
               //setPB
           }
           override fun onSuccess(data: DataSnapshot) {
               val user = data.getValue(User::class.java)
               saveToDB(userReview,user!!)
           }
           override fun onFailed(databaseError: DatabaseError) {
               Toast.makeText(this@ReviewActivity,"Something wrong happened!, try again later.",Toast.LENGTH_SHORT).show()
           }



       })
    }
    private fun saveToDB(userReview: UserReview,user: User){
        if (user.userReviews[mDatabase.currentUserId()] != null){
            val userReviewMap= user.userReviews[mDatabase.currentUserId()] as HashMap<*, *>
            //todo add review button on userprofile
            user.userRatingCount= user.userRatingCount - 1
            user.rawRating = user.rawRating - (userReviewMap["globalRating"].toString().toInt())
            val tRatingHM = userReviewMap["trating"].toString().toInt()
            val sRatingHM = userReviewMap["srating"].toString().toInt()
            val cRatingHM = userReviewMap["crating"].toString().toInt()
            if (tRatingHM != 0){
                user.tRatingCount = user.tRatingCount - 1
                user.rawTRating = user.rawTRating  -  tRatingHM
            }
            if (sRatingHM != 0){
                user.sRatingCount = user.sRatingCount - 1
                user.rawSRating = user.rawSRating  -  sRatingHM
            }
            if (cRatingHM != 0){
                user.cRatingCount = user.cRatingCount - 1
                user.rawCRating = user.rawCRating  -  cRatingHM
            }

        }
        var path = "users/$userID/userReviews/${mDatabase.currentUserId()}"
        mDatabase.addToPath(path,userReview)
        path = "$path/timestamp"
        mDatabase.addToPath(path,Date().toString())
        val rawRating = user.rawRating + userReview.globalRating
        val userRatingCount = user.userRatingCount+1
        val userRating =rawRating / userRatingCount
        //val ratingRef = database.child("users").child(userID)
        val userPath = "users/${mDatabase.currentUserId()}"
        mDatabase.addToPath("$userPath/userRating",userRating)
     //   ratingRef.child("userRating").setValue(userRating)
        mDatabase.addToPath("$userPath/rawRating",rawRating)
     //   ratingRef.child("rawRating").setValue(rawRating)
        mDatabase.addToPath("$userPath/userRatingCount",userRatingCount)
     //   ratingRef.child("userRatingCount").setValue(userRatingCount)
        if (userReview.tRating != 0f){
            val rawTRating = user.rawTRating + userReview.tRating
            val tRatingCount = user.tRatingCount + 1
            val tRating = rawTRating / tRatingCount
            mDatabase.addToPath("$userPath/tRating",tRating)
           // ratingRef.child("tRating").setValue(tRating)
            mDatabase.addToPath("$userPath/rawTRating",rawTRating)
            //ratingRef.child("rawTRating").setValue(rawTRating)
            mDatabase.addToPath("$userPath/tRatingCount",tRatingCount)
            //ratingRef.child("tRatingCount").setValue(tRatingCount)
        }
        if (userReview.sRating != 0f){
            val rawSRating = user.rawSRating + userReview.sRating
            val sRatingCount = user.sRatingCount + 1
            val sRating = rawSRating / sRatingCount
            mDatabase.addToPath("$userPath/sRating",sRating)
           // ratingRef.child("sRating").setValue(sRating)
            mDatabase.addToPath("$userPath/rawSRating",rawSRating)
          //  ratingRef.child("rawSRating").setValue(rawSRating)
            mDatabase.addToPath("$userPath/sRatingCount",sRatingCount)
          // ratingRef.child("sRatingCount").setValue(sRatingCount)
        }
        if (userReview.cRating != 0f){
            val rawCRating = user.rawCRating + userReview.cRating
            val cRatingCount = user.cRatingCount + 1
            val cRating = rawCRating / cRatingCount
            mDatabase.addToPath("$userPath/cRating",cRating)
           // ratingRef.child("cRating").setValue(cRating)
            mDatabase.addToPath("$userPath/rawCRating",rawCRating)
          //  ratingRef.child("rawCRating").setValue(rawCRating)
            mDatabase.addToPath("$userPath/cRatingCount",cRatingCount)
           // ratingRef.child("cRatingCount").setValue(cRatingCount)
        }
    }
    private fun postUserReview(){
        if (userID!=""){
            if(arrayOfStars[0].contentDescription.toString().toUpperCase() == "FULL"){
                val userReview = collectRatings()
                fetchUser(userReview)
                Toast.makeText(this,"Review Posted!",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Please rate the driver first!",Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this,"Something wrong happened, try again later.",Toast.LENGTH_SHORT).show()
        }

    }
    private fun collectRatings():UserReview{
        val globalRating: Float = calculateRating(arrayOfStars)
        val tRating = calculateRating(arrayOfTStars)
        val sRating = calculateRating(arrayOfSStars)
        val cRating = calculateRating(arrayOfCStars)
        val review:String = editText_Review.text.toString()
        val userReview = UserReview(globalRating)
        userReview.tRating = tRating
        userReview.sRating = sRating
        userReview.cRating = cRating
        userReview.review = review
        return userReview
    }
    private fun calculateRating(mArrayOfStars:ArrayList<ImageView>):Float{
        var rating = 0f
        if (mArrayOfStars[0].contentDescription.toString().toUpperCase() == "FULL"){
        for (star in mArrayOfStars){
            if (star.contentDescription.toString().toUpperCase() == "FULL"){
                rating += 1
            }
        }
        }
        return rating
    }
    private fun clearRatingStars(mArrayOfStars:ArrayList<ImageView>){
        for (star in mArrayOfStars) {
            star.contentDescription = "EMPTY"
           star.image = getDrawable(R.drawable.ic_star_border_black_35dp)
        }
    }
    private fun setupRatingStars(mArrayOfStars:ArrayList<ImageView>){
        for ((index,star) in mArrayOfStars.withIndex()){
            star.setOnClickListener {
                for (i in 0..index){
                    mArrayOfStars[i].contentDescription = "FULL"
                    mArrayOfStars[i].image=getDrawable(R.drawable.ic_star_black_35dp)
                }
                if (index != mArrayOfStars.size){
                    for (j in index+1 until mArrayOfStars.size){
                        mArrayOfStars[j].contentDescription = "EMPTY"
                        mArrayOfStars[j].image = getDrawable(R.drawable.ic_star_border_black_35dp)
                    }
                }

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_send_review, menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_send -> {
               postUserReview()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}