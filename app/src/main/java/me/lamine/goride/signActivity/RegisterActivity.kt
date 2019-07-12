package me.lamine.goride.signActivity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import me.lamine.goride.R
import me.lamine.goride.mainActivities.MainActivity
import me.lamine.goride.utils.verifyAvailableNetwork


class RegisterActivity : AppCompatActivity() {
    //private val TAG: String = "RegisterActivity"
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(register_tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        register_tb.navigationIcon?.setColorFilter(ContextCompat.getColor(this,R.color.whiteColor), PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.title = ""
        mAuth = FirebaseAuth.getInstance()
        if (!verifyAvailableNetwork(this)){
            Toast.makeText(this,"No Available Network.", Toast.LENGTH_SHORT).show()
        }
        nextRegisterBtn.setOnClickListener {
            val email = remail_edittext.text.toString()
            val password = rpassword_edittext.text.toString()
            val displayName = rname_edittext.text.toString()
            createNewAccount(email, password, displayName)
        }
        loading_btn_register.setOnClickListener {
            val email = remail_edittext.text.toString()
            val password = rpassword_edittext.text.toString()
            val displayName = rname_edittext.text.toString()
            remail_label.removeError()
            rpassword_label.removeError()
            createNewAccount(email, password, displayName)
        }
        login_textbtn.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            updateInterface(currentUser)
        }
        // updateUI(currentUser)
    }

    private fun updateInterface(user: FirebaseUser?) {
        val intent = Intent(this, RegisterExtraActivity::class.java)
        intent.putExtra("User", user)
        startActivity(intent)
        finish()
    }

    private fun checkFields(email: String, password: String, displayName: String): Boolean {
        var mBool = true
        if (email.isEmpty()) {
            remail_label.setError("Email can't be empty.", false)
            mBool = false
        }
        if (displayName.isEmpty()) {
            rname_label.setError("Fullname can't be empty", false)
            mBool = false
        }
        if (password.isEmpty()) {
            rpassword_label.setError("Password can't be empty", false)
            mBool = false
        }
        return mBool
    }
    private fun createNewAccount(email: String, password: String, displayName: String) {
        if (!checkFields(email, password, displayName)) {
            Toast.makeText(
                this, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            loading_btn_register.startLoading()
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build()

            mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth?.currentUser
                    user?.updateProfile(profileUpdates)
                    loading_btn_register.loadingSuccessful()
                    updateInterface(user)
                } else {
                    // If sign in fails, display a message to the user.
                    loading_btn_register.loadingFailed()
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this, "Authentication failed. ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    when {
                        task.exception?.localizedMessage!!.toLowerCase().contains("email") -> remail_label.setError("Invalid Email",false)
                        task.exception?.localizedMessage!!.toLowerCase().contains("password") -> rpassword_label.setError("Invalid Password",false)
                        else -> rpassword_edittext.error = "Error, Verify your email and password."
                    }
                    //  updateUI(null)
                }

                // ...
            }
        }
    }
}