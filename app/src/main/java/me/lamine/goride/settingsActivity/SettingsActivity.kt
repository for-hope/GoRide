package me.lamine.goride.settingsActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import me.lamine.goride.R
import me.lamine.goride.utils.Database


class SettingsActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        modify_profile_setting.setOnClickListener {
            val i = Intent(this,ModifyProfileActivity::class.java)
            startActivity(i)
        }
        notif_switch_setting.isChecked = getNotificationSettings()
        notif_switch_setting.setOnCheckedChangeListener { _, isChecked ->
            setNotificationSettings(isChecked)
        }
        privacy_setting.setOnClickListener {
            val i = Intent(this,TextActivity::class.java)
            i.putExtra("title","Privacy Policy")
            startActivity(i)
        }
        about_setting.setOnClickListener {
            val i = Intent(this,TextActivity::class.java)
            i.putExtra("title","About")
            startActivity(i)
        }
        terms_setting.setOnClickListener {
            val i = Intent(this,TextActivity::class.java)
            i.putExtra("title","Terms & Conditions")
            startActivity(i)
        }

        logout_setting.setOnClickListener {
            Database().signOut(this)
        }


    }
    private fun setNotificationSettings(isChecked:Boolean) {
        val mPrefs = this.getSharedPreferences("Settings", Context.MODE_PRIVATE)!!
        mPrefs.edit().putBoolean("notifications",isChecked).apply()
    }
    private fun getNotificationSettings():Boolean{
        val mPrefs = this.getSharedPreferences("Settings", Context.MODE_PRIVATE)!!
        return mPrefs.getBoolean("notifications",true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}