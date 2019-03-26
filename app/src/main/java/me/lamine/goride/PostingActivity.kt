package me.lamine.goride

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PostingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_something)
        setSupportActionBar(findViewById(R.id.my_toolbar2))
    }


}