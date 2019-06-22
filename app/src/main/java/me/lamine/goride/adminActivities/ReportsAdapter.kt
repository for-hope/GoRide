package me.lamine.goride.adminActivities



import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Report
import me.lamine.goride.utils.Database
import java.text.SimpleDateFormat
import java.util.*


class ReportsAdapter(private var context: Context, private var reports: MutableList<Report>) :
    RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {
    private lateinit var mDatabase: Database
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        val tripIdStr = "TRIP_ID = ${report.tripId}"
        holder.tripId.text = tripIdStr
        val reportDescription = "Report : '''${report.report}'''"
        holder.report.text = reportDescription
        val reporterId = "Reporter ID : ${report.reporterId}"
        holder.reporterId.text = reporterId
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val mDate = Date(report.timestamp)
        val dateStamp = "Report Date : ${sdf.format(mDate)}"
        holder.timestamp.text = dateStamp
    }

   /* private fun reloadFragment() {
        // Reload current fragment
        val act = context as Activity
        act.finish()
        act.startActivity(act.intent)
    }*/

    private fun removeReport(pos: Int) {
        val item = reports[pos]
        reports.remove(item)
        notifyDataSetChanged()


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.notif_standered_layout, parent, false)
        return ReportViewHolder(view).listen {pos,type ->
            removeReport(pos)
        }
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    open class ReportViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var tripId: TextView = v.findViewById(R.id.notif_date)
        var report: TextView = v.findViewById(R.id.notif_desc1)
        var reporterId: TextView = v.findViewById(R.id.notif_origin)
        val timestamp: TextView = v.findViewById(R.id.timestamp_notif)
        val notifIcon: ImageView = v.findViewById(R.id.notif_ic)


    }
}