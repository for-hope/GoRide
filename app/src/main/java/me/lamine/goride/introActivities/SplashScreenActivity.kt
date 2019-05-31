package me.lamine.goride.introActivities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import me.lamine.goride.mainActivities.MainActivity
import me.lamine.goride.signActivity.LoginActivity


class SplashScreenActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!getUserIsLogged()) {
            val i = Intent(this,LoginActivity::class.java)
            startActivity(i)
            finish()
        }else {
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            this@SplashScreenActivity.finish()
        }
    }
    private fun getUserIsLogged():Boolean{
        val mPref = this.getSharedPreferences("UserPref", Context.MODE_PRIVATE)!!
        return mPref.getBoolean("isLogged",false)
    }
}