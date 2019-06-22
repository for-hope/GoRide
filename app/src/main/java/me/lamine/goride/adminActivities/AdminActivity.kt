package me.lamine.goride.adminActivities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.fragment_driver.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Report
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database

class AdminActivity:AppCompatActivity(){
    private lateinit var mDatabase: Database
    private var listOfReports:MutableList<Report> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        setSupportActionBar(admin_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = Database()
        Log.i("line22","True")
        fetchReports()
        fab_clear_reports.hide()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun fetchReports(){
        Log.i("line26","True")
        listOfReports = mutableListOf()
        mDatabase.fetchReport(object:OnGetDataListener{
            override fun onStart() {
                Log.i("line30","True")
               admin_pb.visibility = View.VISIBLE
                admin_pb.isIndeterminate = true

            }

            override fun onSuccess(data: DataSnapshot) {
                Log.i("line37","True")
                admin_pb.visibility = View.GONE
                Log.i("reportsw","True")
                     for (ds in data.children){
                         Log.i("reports","True")
                         val report = ds.getValue(Report::class.java)
                         listOfReports.add(report!!)
                         if (ds.key == data.children.last().key){
                             Log.i("setAdapter0","True")
                             setAdapter()
                         }
                     }
            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })
    }
    private fun setAdapter(){
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        list_of_reports.layoutManager = llm
      list_of_reports.adapter = ReportsAdapter(this,listOfReports)
        if (listOfReports.isNotEmpty()){
            fab_clear_reports.show()
            fab_clear_reports.setOnClickListener {
                listOfReports.clear()
                val  mAdapter = list_of_reports.adapter as ReportsAdapter
                mAdapter.notifyDataSetChanged()
                Log.i("Admin","Clearing Reports")
            }
        }
    }
}