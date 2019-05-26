package me.lamine.goride.postingActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_post_something.*
import me.lamine.goride.R
import me.lamine.goride.requestActivity.RequestTripActivity


class PostOptionsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_something)
        setSupportActionBar(post_option_tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
         val postARideCard = findViewById<CardView>(R.id.card_img)
        postARideCard.setOnClickListener {
            Log.i("Options","ACTIVITY STARTED")
            val intent = Intent(this, PostingActivity::class.java)
            startActivity(intent)
            this.finish()
            }
        val postARequest = findViewById<CardView>(R.id.card_img2)
        postARequest.setOnClickListener {
            val intent = Intent(this,RequestTripActivity::class.java)
            startActivity(intent)
            this.finish()
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}