package me.lamine.goride.userActivity


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Review
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.inboxActivity.MessageActivity
import me.lamine.goride.reviewActivity.ReviewActivity
import me.lamine.goride.settingsActivity.ModifyProfileActivity
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.saveSharedUser
import org.jetbrains.anko.textColor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {
    private val mGALLERY = 1
    private val mCAMERA = 2
    private var downloadUrl:String = ""
    private lateinit var mDatabase:Database
    private lateinit var mUser: User
    private lateinit var mStorageRef: StorageReference
    private var  listOfReviews: MutableList<Review> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(user_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = Database()
        mStorageRef = FirebaseStorage.getInstance().reference
        mUser = intent.getSerializableExtra("UserProfile") as User
        if (mUser.userId == mDatabase.currentUserId()){
            btn_change_pfp.visibility = View.VISIBLE
            btn_change_pfp.setOnClickListener {
                    showImageDialog()
            }
        } else {
            btn_change_pfp.visibility = View.GONE
        }
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
        val date = Date(mUser.accountCreatingDate)
        val sdf = SimpleDateFormat("dd MMMM yyy", Locale.US)
        val mDateString = sdf.format(date)

        user_ac_joined.text = mDateString
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
        if (mUser.userId == Database().currentUserId()){
            inflater.inflate(R.menu.menu_edit_profile, menu)
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
            R.id.action_edit_profile -> {
                Log.i("UserAct","clicked edit profile")
                val intent = Intent(this, ModifyProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    Log.i("Reviwer", "added")
                    review.reviewUser = mUser
                    if (review == listOfReviews.last()) {
                        for (rev in listOfReviews){
                            Log.i("REVIEW : ", " L =" + rev.reviewUser?.fullName)
                        }
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
    companion object {
        private const val IMAGE_DIRECTORY = "/RideGO_Images"
    }


    private  fun showImageDialog(){
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ////
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == mGALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //val path = saveImage(bitmap)
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
                    //carBtn.setImageBitmap(bitmap)
                    user_imageview.setImageBitmap(bitmap)
                    savePhotoToDatabase(user_imageview)
                    //todo bool

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == mCAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            // carBtn.setImageBitmap(thumbnail)
            user_imageview.setImageBitmap(thumbnail)
            //todo setup database
            savePhotoToDatabase(user_imageview)
            saveImage(thumbnail)
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + UserActivity.IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .timeInMillis).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }
    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, mGALLERY)
    }
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, mCAMERA)
    }
    private fun savePhotoToDatabase(imageView: ImageView) {
        // val file = Uri.fromFile(File("path/to/images/rivers.jpg"))
        val uniqueId = UUID.randomUUID().toString()
        val riversRef = mStorageRef.child("user_images/$uniqueId.jpg")
// Get the data from an ImageView as bytes

        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = riversRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.i("UPLOAD", "FAILED")
        }.addOnSuccessListener { it1 ->
            val result = it1.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener {
                downloadUrl = it.toString()
                val userPath = "users/${mDatabase.currentUserId()}/profilePic"
                mDatabase.addToPath(userPath, downloadUrl)
                mUser.profilePic = downloadUrl
                saveSharedUser(this,mUser)
                //downloadUrl = uniqueId
            }

        }

    }
}