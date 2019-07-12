package me.lamine.goride.introActivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_intro.*
import me.lamine.goride.R
import me.lamine.goride.signActivity.LoginActivity

class StartActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        start_email_btn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}