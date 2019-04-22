package me.lamine.goride

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseUser


class LoginActivity:AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener {
            val email = email_edittext.text.toString()
            val password = password_edittext.text.toString()
            signUserIn(email,password)
        }



    }
    private fun updateUI(user:FirebaseUser){
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("User",user)
        startActivity(intent)
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        //updateUI(currentUser)
    }
    private fun signUserIn(email:String, password:String){
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInWithEmail:success")
                val user = mAuth?.currentUser
                // updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w("TAG", "signInWithEmail:failure", task.exception)
                Toast.makeText(
                    this, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
                // updateUI(null)
            }

            // ...
        }
    }
}