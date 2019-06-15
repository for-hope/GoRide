package me.lamine.goride.settingsActivity

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import kotlinx.android.synthetic.main.activity_modify_profile.*
import kotlinx.android.synthetic.main.activity_modify_profile.age_label
import kotlinx.android.synthetic.main.activity_user.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database
import org.jetbrains.anko.onCheckedChange
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ModifyProfileActivity:AppCompatActivity() {
    private lateinit var database:Database
    private var mUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile)
        setSupportActionBar(modify_profile_tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        database = Database()
        Log.i("ModifyActivity","started")
         setInfo()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun setInfo(){
        database.fetchUser(database.currentUserId(),object :OnGetDataListener{
            override fun onStart() {
                Log.i("ModifyActivity","onStart")
            }

            override fun onSuccess(data: DataSnapshot) {
                Log.i("ModifyActivity","onSuc")
                mUser = data.getValue(User::class.java)
                if (mUser != null){
                    edittext_fullname.setText(mUser?.fullName)
                    edittext_age.setText(mUser?.birthday)
                    edittext_desc.setText(mUser?.description)
                    driver_checkbox.isChecked = mUser?.isDriver!!
                    edittext_email.setText(mUser?.email)
                    initViews()
                    confirm_btn.setOnClickListener {
                        updateInfo()
                    }


                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("ModifyActivity","onFaield")
                Toast.makeText(this@ModifyProfileActivity,"Error occurred : ${databaseError.message}",Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun updateInfo(){
        if (edittext_fullname.text.toString() != "" && edittext_fullname.text.toString() != mUser?.fullName){
            database.addToPath("users/${database.currentUserId()}/fullName",edittext_fullname.text.toString())
        }
        if (edittext_age.text.toString() != "" && edittext_fullname.text.toString() != mUser?.birthday){
            database.addToPath("users/${database.currentUserId()}/birthday",edittext_age.text.toString())
        }
        if (edittext_desc.text.toString() != mUser?.description){
            database.addToPath("users/${database.currentUserId()}/description",edittext_desc.text.toString())
        }
        if (driver_checkbox.isChecked != mUser?.isDriver){
            database.addToPath("users/${database.currentUserId()}/isDriver",driver_checkbox.isChecked)
        }
        if (edittext_email.text.toString() != mUser?.email){
            database.addToPath("users/${database.currentUserId()}/email",edittext_email.text.toString())
            database.getFirebaseUser()?.updateEmail(edittext_email.text.toString())
        }
        if (edittext_pass.text.toString().length > 6){
            database.getFirebaseUser()?.updatePassword(edittext_pass.text.toString())
        } else {
            password_label.setError("password must be more than 6 character",true)
        }
        showDoneDialog()
    }

    private fun showDoneDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile Saved!")
            .setMessage("Your changes have been saved successfully")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Done") { _, _ ->

                Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()
                finish()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_done_green_24dp)
            .show()
    }
    private fun initViews(){
        age_label.setOnClickListener {
            val c = Calendar.getInstance()
            val myYear = c.get(Calendar.YEAR)
            val myMonth = c.get(Calendar.MONTH)
            val myDay = c.get(Calendar.DAY_OF_MONTH)
            val listener =
                com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val myFormat = "dd, MMMM yyyy"
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    c.set(year, monthOfYear, dayOfMonth)
                    edittext_age.setText(sdf.format(c.time))
                    if (getAge(edittext_age.text.toString()) < 60){
                        driver_checkbox.isChecked = true
                        driver_checkbox.onCheckedChange { compoundButton, b ->
                        if (getAge(edittext_age.text.toString()) < 60){
                         driver_checkbox.isChecked = true
                            }
                        }
                    }

                }
            SpinnerDatePickerDialogBuilder()
                .context(this)
                .callback(listener)
                .spinnerTheme(R.style.NumberPickerStyle)
                .showTitle(true)
                .showDaySpinner(true)
                .defaultDate(myYear, myMonth, myDay)
                .maxDate(myYear - 18, myMonth, myDay)
                .minDate(1900, myMonth, myDay)
                .build()
                .show()
        }


    }

    private fun getAge(dobString: String): Int {

        var date: Date? = null
        val sdf = SimpleDateFormat("dd, MMMM yyyy", Locale.US)
        try {
            date = sdf.parse(dobString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (date == null) return 0

        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.time = date

        val year = dob.get(Calendar.YEAR)
        val month = dob.get(Calendar.MONTH)
        val day = dob.get(Calendar.DAY_OF_MONTH)

        dob.set(year, month + 1, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }



        return age
    }
}