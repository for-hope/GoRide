package me.lamine.goride.userActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.LiteUser
import java.text.SimpleDateFormat
import java.util.*

class UserActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        user_toolbar.title = "User"
        setSupportActionBar(user_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val liteUser = intent.getSerializableExtra("UserProfile") as LiteUser
        //setup profile picture
        Picasso.get().load(liteUser.profilePic).into(user_imageview)
        //setup name
        user_ac_username.text = liteUser.name
        //gender and age
        val age_format = "dd, MMM yyyy"
        val userBirthday = liteUser.birthday
        val df = SimpleDateFormat(age_format, Locale.US)
        val uBirthdate = df.parse(userBirthday)
        val cal = Calendar.getInstance()
        cal.time = uBirthdate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        val age = getAge(year, month, day)
        var gender = liteUser.gender.capitalize()
        gender = if (gender=="F"){
            "Female"
        } else {
            "Male"
        }
        val genderAndAge = "$gender, $age"
        user_ac_genderAge.text = genderAndAge

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
}