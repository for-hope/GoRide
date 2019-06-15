package me.lamine.goride.introActivities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import me.lamine.goride.mainActivities.MainActivity
import me.lamine.goride.signActivity.LoginActivity
import me.lamine.goride.utils.Database


class SplashScreenActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            this@SplashScreenActivity.finish()
        } else {
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)
            this@SplashScreenActivity.finish()
        }

    }

}