package me.lamine.goride.signActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_register.*
import me.lamine.goride.R


class RegisterActivity : AppCompatActivity() {
    //private val TAG: String = "RegisterActivity"
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        nextRegisterBtn.setOnClickListener {
            val email = remail_edittext.text.toString()
            val password = rpassword_edittext.text.toString()
            val displayName = rname_edittext.text.toString()
            createNewAccount(email, password, displayName)
        }
        login_textbtn.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }


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
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build()
            mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FragmentActivity.TAG", "createUserWithEmail:success")
                    val user = mAuth?.currentUser
                    user?.updateProfile(profileUpdates)
                    //  Log.i("User ANME", user?.displayName)
                    updateInterface(user)
                } else {
                    // If sign in fails, display a message to the user.

                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this, "Authentication failed. ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    //  updateUI(null)
                }

                // ...
            }
        }
    }
}