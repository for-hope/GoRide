package me.lamine.goride.dataObjects

import java.util.*


class Report (
    var report: String = "",
    var reporterId: String = "",
    var tripId: String = ""
){
var timestamp: Long = Date().time
}