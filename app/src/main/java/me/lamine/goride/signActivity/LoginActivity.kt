package me.lamine.goride.signActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import me.lamine.goride.R
import me.lamine.goride.mainActivities.MainActivity


class LoginActivity:AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance();
        create_account.setOnClickListener { Log.i("Login","CLICKED")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()}
        btnLogin.setOnClickListener {
            val email = email_edittext.text.toString()
            val password = password_edittext.text.toString()
            signUserIn(email,password)
        }



    }
    private fun updateUI(user:FirebaseUser?){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User",user)
        startActivity(intent)
        finish()
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        if (currentUser!=null) {
            updateUI(currentUser)
        }
    }
    private fun signUserIn(email:String, password:String){
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInWithEmail:success")
                val user = mAuth?.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w("TAG", "signInWithEmail:failure", task.exception)
                when {
                    task.exception is FirebaseAuthInvalidUserException -> Toast.makeText(
                        this, "User doesn't exist. check your email.",
                        Toast.LENGTH_SHORT
                    ).show()
                    task.exception is FirebaseAuthInvalidCredentialsException -> Toast.makeText(
                        this, "Incorrect password.",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }



                //todo updateUI(null)
            }

            // ...
        }
    }
}