package com.yogeshj.myFitness.trainer

class Trainer {
    var uid:String?=null
    var name:String?=null
    var email:String?=null
    var meetingTime:String?=null
    var meetingDate:String?=null
    var fees:Int?=0
    var meetingLink:String?=null

    constructor()

    constructor(uid:String,name:String,email: String,fees:Int){
        this.uid=uid
        this.name=name
        this.email=email
        this.fees=fees
        this.meetingTime=null
        this.meetingDate=null
    }
    constructor(uid:String,name:String,email: String,fees:Int,meetingTime:String,meetingDate:String){
        this.uid=uid
        this.name=name
        this.email=email
        this.fees=fees
        this.meetingTime=meetingTime
        this.meetingDate=meetingDate
    }

}