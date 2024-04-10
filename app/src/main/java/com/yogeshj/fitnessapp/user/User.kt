package com.yogeshj.fitnessapp.user

class User {
    var uid:String?=null
    var name:String?=null
    var email:String?=null

    constructor()

    constructor(uid:String,name:String,email: String){
        this.uid=uid
        this.name=name
        this.email=email
    }
}