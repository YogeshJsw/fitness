package com.yogeshj.fitnessapp


data class PaymentData(
    val userId: String? = null,
    val trainerId: String? = null,
    val amount: Int = 0,
    val paymentStatus: String? = null,
    val meetingTime:String?=null,
    val meetingDate:String?=null,
    val email:String?=null

)

//class PaymentData {
//    private var userId: String? = null
//    private var trainerId: String? = null
//    private var paymentAmount: String? = null
//    private var paymentStatus: String? = null
//
//
//    constructor(userId:String,trainerId:String,paymentAmount:String,paymentStatus:String)
//    {
//        this.userId=userId
//        this.trainerId=trainerId
//        this.paymentAmount=paymentAmount
//        this.paymentStatus=paymentStatus
//    }
//}