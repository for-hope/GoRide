package me.lamine.goride.signActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_login.*
import me.lamine.goride.R
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.javaClasses.RevealAnimation
import me.lamine.goride.mainActivities.MainActivity
import me.lamine.goride.utils.Database


class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        password_label.setEndIcon(R.drawable.ic_visibility_gray_24dp)
        password_label.endIconImageButton
        password_label.endIconImageButton.setOnClickListener {
            if (password_label.endIconResourceId == R.drawable.ic_visibility_gray_24dp){

                password_edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                password_label.setEndIcon(R.drawable.ic_visibility_off_gray_24dp)
            } else {
                password_edittext.inputType =  InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                password_label.setEndIcon(R.drawable.ic_visibility_gray_24dp)
            }

        }
        create_account.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        loading_btn.setOnClickListener {
            val email = email_edittext.text.toString()
            val password = password_edittext.text.toString()
            signUserIn(it, email, password)

        }
    }

    private fun startRevealActivity(v: View, user: FirebaseUser?) {
        //calculates the center of the View v you are passing
        val revealX = (v.x + v.width / 2).toInt()
        val revealY = (v.y + v.height / 2).toInt()

        //create an intent, that launches the second activity and pass the x and y coordinates
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_X, revealX)
        intent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y, revealY)
        intent.putExtra("User", user)

        //just start the activity as an shared transition, but set the options bundle to null
        ActivityCompat.startActivity(this, intent, null)

        //to prevent strange behaviours override the pending transitions
        overridePendingTransition(0, 0)
    }

    private fun updateUI(v: View?, user: FirebaseUser?) {
        Database().fetchUser(user?.uid!!, object : OnGetDataListener {
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
                loading_btn.loadingSuccessful()
                if (data.exists()) {
                    if (v != null) {
                        startRevealActivity(v, user)
                    } else {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("User", user)
                        startActivity(intent)
                    }

                    finish()
                } else {
                    val intent = Intent(this@LoginActivity, RegisterExtraActivity::class.java)
                    intent.putExtra("User", user)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Error starting activity, try again.", Toast.LENGTH_LONG).show()
                this@LoginActivity.finish()
            }

        })


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            updateUI(null, currentUser)
        }
    }

    private fun signUserIn(v: View, email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            password_label.removeError()
            email_label.removeError()
            loading_btn.startLoading()
            mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    val user = mAuth?.currentUser

                    updateUI(v, user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    when {
                        task.exception is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                this, "User doesn't exist. check your email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            email_label.setError("Account doesn't exist. check your email.", false)
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(
                                this, "Invalid password.",
                                Toast.LENGTH_SHORT
                            ).show()
                            password_label.setError("Invalid password", false)
                        }
                        else -> Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    loading_btn.loadingFailed()
                }

                // ...
            }
        } else {
            password_label.setError("Fields cannot be empty.", true)
        }
    }
}