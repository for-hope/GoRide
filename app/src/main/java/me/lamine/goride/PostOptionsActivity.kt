package me.lamine.goride
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView



class PostOptionsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_something)
         val postARideCard = findViewById<CardView>(R.id.card_img)
        postARideCard.setOnClickListener {  val intent = Intent(this, PostingActivity::class.java)
            startActivity(intent)
        this.finish()}

    }
}