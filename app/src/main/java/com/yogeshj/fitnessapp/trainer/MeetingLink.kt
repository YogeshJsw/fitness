package com.yogeshj.fitnessapp.trainer

class MeetingLink {
    var trainerId:String?=null
     var meetingLink:String?=null

    constructor()

    constructor(trainerId:String,meetingLink: String)
    {
        this.trainerId=trainerId
        this.meetingLink=meetingLink
    }

}