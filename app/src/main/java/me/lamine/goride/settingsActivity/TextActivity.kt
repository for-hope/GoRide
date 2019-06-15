package me.lamine.goride.settingsActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_text.*
import me.lamine.goride.R

class TextActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar_text)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_text)
        val mTitle = intent.getStringExtra("title")
        var mContent = ""
        when (mTitle) {
            "Privacy Policy" -> mContent = getString(R.string.PrivacyPolicy)
            "Terms & Conditions" -> mContent = getString(R.string.TermsConditions)
            "About" -> mContent = getString(R.string.About)
        }

        text_title.text = mTitle
        text_content.text = mContent
        actionBar?.title = mTitle

    }
}