package me.lamine.goride.notifications

data class Data(var user:String, var icon:Int = 0, var body:String, var sented:String) {
    constructor() : this("",0,"","")
}