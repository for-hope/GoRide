package me.lamine.goride.signActivity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_extra_register.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import me.lamine.goride.R
import me.lamine.goride.dataObjects.User
import me.lamine.goride.mainActivities.MainActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit





class RegisterExtraActivity: AppCompatActivity() {
    private val GALLERY = 1
    private val CAMERA = 2
    private var isImageSet = false
    private var downloadUrl = ""
    private lateinit var mStorageRef: StorageReference
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private lateinit var email:String
    private lateinit var fullName:String
    private lateinit var phoneNumber:String
    private lateinit var birthday:String
    private lateinit var gender:String
    private var isDriver:Boolean = false
    private lateinit var description:String
    private lateinit var database: DatabaseReference
    private lateinit var userId:String
    private var mAuth: FirebaseAuth? = null
    private var currentUser:FirebaseUser? = null
    private var TAG = "RegisterExtraActvity"
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extra_register)
        setSupportActionBar(extra_reg_toolbar)
        extra_reg_toolbar.changeToolbarFont()
        database = FirebaseDatabase.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().reference;
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Log.i("RegisterExtra", "Finish")
            finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        initCallBacks()
        progressBar_extra.progress = 20
        age_label.onClick {
            Toast.makeText(this,"CLicked",Toast.LENGTH_SHORT)
            val c = Calendar.getInstance()
            val myYear = c.get(Calendar.YEAR)
            val myMonth = c.get(Calendar.MONTH)
            val myDay = c.get(Calendar.DAY_OF_MONTH)
            val listener = com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val myFormat = "dd, MMMM yyyy"
                val sdf  = SimpleDateFormat(myFormat, Locale.US)
                c.set(year,monthOfYear,dayOfMonth)
                age_edittext.setText(sdf.format(c.time))
            }
            SpinnerDatePickerDialogBuilder()
                .context(this)
                .callback(listener)
                .spinnerTheme(R.style.NumberPickerStyle)
                .showTitle(true)
                .showDaySpinner(true)
                .defaultDate(myYear, myMonth, myDay)
                .maxDate(myYear-18, myMonth, myDay)
                .minDate(1900, myMonth, myDay)
                .build()
                .show()

        }
       male_checkbox.isChecked = true
       male_checkbox.setOnCheckedChangeListener { _, isChecked ->
           if (isChecked){
               female_checkbox.isChecked = false
           }
       }
        female_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                male_checkbox.isChecked = false
            }
        }
        selfie_btn.setOnClickListener { takePhotoFromCamera() }
        gallery_btn.setOnClickListener { choosePhotoFromGallery() }
        //val viewFlipper = view_flipper
//        viewFlipper.addView(phone_verf_layout)
        next_button.setOnClickListener {
            Log.i("TAG","${progressBar_extra.progress}")
            when {
                progressBar_extra.progress == 20 -> onVerifyPhoneNumber()
                progressBar_extra.progress == 40 -> onVerifyCode()
                progressBar_extra.progress == 60 -> checkBirthdayAndGender()
                progressBar_extra.progress == 80 -> userTypeAndDescription()
                progressBar_extra.progress == 100 -> acceptTerms()
                else -> Log.i("TAG", "LOL")//nextView(view_flipper)
            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ////
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //val path = saveImage(bitmap)
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
                    //carBtn.setImageBitmap(bitmap)
                    reg_pfp.setImageBitmap(bitmap)
                    isImageSet = true

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
           // carBtn.setImageBitmap(thumbnail)
            reg_pfp.setImageBitmap(thumbnail)
            saveImage(thumbnail)
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        Log.d("fee",wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists())
        {

            wallpaperDirectory.mkdirs()
        }

        try
        {
            Log.d("heel",wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                .timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        }
        catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }
    companion object {
        private const val IMAGE_DIRECTORY = "/RideGO_Images"
    }
    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
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

    private fun nextView(viewFlipper:ViewFlipper){
        progressBar_extra.progress = progressBar_extra.progress + 20
        Log.i("prog", "${progressBar_extra.progress}")
        viewFlipper.showNext()
        if (progressBar_extra.progress == 100){
            next_button.text = "Finish"
        }

    }
    private fun onVerifyPhoneNumber(){
        phoneNumber= phone_nb_edittext.text.toString()
        val correctNumber:Boolean = phoneNumber.length == 9 &&
                (phoneNumber.startsWith("5") || phoneNumber.startsWith("6") || phoneNumber.startsWith("7"))
        if (correctNumber) {
            phoneNumber = "+213$phoneNumber"
            verifyPhoneNumberFirebase(phoneNumber)
        } else {
           phone_nb_label.setError("Please enter a valid number.",true)
           Toast.makeText(this,"Incorrect Number",Toast.LENGTH_SHORT).show()
        }
    }
    private fun initCallBacks(){
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(
                verificationId: String?,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // ...
            }
        }

    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        setPb(1)
        currentUser?.updatePhoneNumber(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                setPb(0)
                nextView(view_flipper)
                // ...
            } else {
                // Sign in failed, display a message and update the UI
                Toast.makeText(this,"Error, try again.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    code_label.setError("Invalid code.",true)

                    // The verification code entered was invalid
                }
            }
        }
    }

    private fun verifyPhoneNumberFirebase(number:String){
        nextView(view_flipper)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,             // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
       // Toast.makeText(this,"Verifying...",Toast.LENGTH_SHORT).show()

    }
    private fun onVerifyCode(){

        val code = code_edittext.text.toString()
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)

    }
    private fun checkBirthdayAndGender(){
        birthday = age_edittext.text.toString()
        gender = "m"
        if (female_checkbox.isChecked){
            gender = "f"
        }
        Toast.makeText(this,"Birthday : $birthday Gender: $gender",Toast.LENGTH_SHORT).show()
        nextView(view_flipper)
    }
    private fun userTypeAndDescription(){
        isDriver = driver_checkbox.isChecked
        description = desc_edittext.text.toString()
        Toast.makeText(this, "isDriver ${isDriver.toString()}, ${description.length.toString()}",Toast.LENGTH_SHORT).show()
        nextView(view_flipper)

    }
    private fun saveUserToDB(){
        Toast.makeText(this,"Saving to database...",Toast.LENGTH_LONG).show()
        val user = User(
            userId,
            email,
            fullName,
            phoneNumber,
            birthday,
            gender,
            description,
            isDriver
        )
        if (downloadUrl == ""){
            downloadUrl = if (user.gender != "f") {
                "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
            } else {
                "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fdefault_female.png?alt=media&token=3cebd02b-e83c-418f-8541-96fa43fbca95"
            }
        }
        user.profilePic = downloadUrl
        database.child("users").child(userId).setValue(user) { databaseError, _ ->
            if (databaseError != null) {
                Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        database.push()
        Toast.makeText(this,"Done.",Toast.LENGTH_LONG).show()
        setPb(0)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun acceptTerms(){
        if (terms_checkbox.isChecked){
           // val uuid = randomUUID()
            if (currentUser != null){
            userId = currentUser!!.uid
            email = currentUser!!.email.toString()
            fullName = currentUser!!.displayName.toString()
            } else {
                Log.i("Error", "NO CURRENT USER")
                finish()
            }
            setPb(1)
            savePhotoToDatabase(reg_pfp)


        } else {
            Toast.makeText(this,"You have to accept terms of service to continue",Toast.LENGTH_LONG).show()
        }
    }
    private fun savePhotoToDatabase(imageView: ImageView){
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
            Log.i("UPLOAD","FAILED")
        }.addOnSuccessListener { it1 ->
            val result = it1.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener {
                downloadUrl = it.toString()
                saveUserToDB()
                //downloadUrl = uniqueId
            }

        }

    }
    private fun setPb(visibility: Int){
        if (visibility == 1) {
           pb_extra_reg.visibility = View.VISIBLE
            grey_extra_layout.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            pb_extra_reg.visibility = View.GONE
            grey_extra_layout.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}