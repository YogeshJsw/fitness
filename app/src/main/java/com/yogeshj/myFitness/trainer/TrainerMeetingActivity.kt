package com.yogeshj.myFitness.trainer

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.PaymentData
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityTrainerMeetingBinding
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Random
import java.util.UUID




class TrainerMeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivityTrainerMeetingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dialog:Dialog
    private lateinit var dbRef:DatabaseReference

    private var mInterstitialAd: InterstitialAd? = null


    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    companion object {
        var savedDay = 0
        var savedMonth = 0
        var savedYear = 0
        var savedHour = 0
        var savedMinute = 0
    }

    lateinit var date:LocalDate
    lateinit var time:LocalTime
    var flag=false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainerMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HomeActivity.auth = FirebaseAuth.getInstance()
        initLoadingDialog()

        showLoading()

        MobileAds.initialize(this@TrainerMeetingActivity) {}
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this@TrainerMeetingActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })

        var paymentTaskComplete = false
        var trainersTaskComplete = false


        val currentDate = LocalDate.now()
        val currentTime=LocalTime.now()

//        Log.d("currentTime","${currentTime}")
        var count=0
        var registeredText:String=""
        dbRef = FirebaseDatabase.getInstance().getReference("Payment")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(PaymentData::class.java)!!
                        if (HomeActivity.auth.currentUser?.uid == curr.trainerId) {
                            registeredText+= "∙${curr.email}\t\t\t-\t\t\tRs${curr.amount}\n"

                        }
                        if(snap!=null)
                            count++
                    }
                    binding.details.text=registeredText
                    binding.registeredUsers.text="Registered Users: $count"
                }
                paymentTaskComplete=true
                if (paymentTaskComplete && trainersTaskComplete) {
                    hideLoading()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                paymentTaskComplete=true
                if (paymentTaskComplete && trainersTaskComplete) {
                    hideLoading()
                }
            }

        })


        dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(Trainer::class.java)!!
                        if (HomeActivity.auth.currentUser?.uid == curr.uid) {
                            binding.heading.text = "Welcome ${curr.name}"
                            if(curr.meetingTime!=null)
                            {
                                flag=true
                                binding.dateAndTime.text="Date: ${curr.meetingDate}, Time: ${curr.meetingTime}"
                            }
                        }
                    }
                }
                trainersTaskComplete=true
                if (paymentTaskComplete && trainersTaskComplete) {
                    hideLoading()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trainersTaskComplete=true
                if (paymentTaskComplete && trainersTaskComplete) {
                    hideLoading()
                }
            }

        })

        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE)
        binding.nameInput.setText(sharedPreferences.getString("name", ""))

        binding.expand.setOnClickListener {
            showLoading()
            expand(binding.details,binding.expand,binding.collapse)
            hideLoading()
        }
        binding.collapse.setOnClickListener {
            showLoading()
            collapse(binding.details,binding.expand,binding.collapse)
            hideLoading()
        }
        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@TrainerMeetingActivity)
            }
            finish()
        }

        binding.set.setOnClickListener {
            if(binding.dateAndTime.text.isNotEmpty())
                Snackbar.make(binding.root,"Meeting already scheduled.",Snackbar.LENGTH_LONG).show()
            else {
                getDateTime()
                DatePickerDialog(this, this, year, month, day).show()
            }
        }

        binding.update.setOnClickListener {
            showLoading()
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var hasMeeting = false
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(Trainer::class.java)!!
                            //Get trainer meeting id from here
                            if(curr.uid==HomeActivity.auth.currentUser?.uid)
                            {
                                if(curr.meetingDate!=null)
                                {
                                    hasMeeting=true
                                    getDateTime()
                                    DatePickerDialog(this@TrainerMeetingActivity, this@TrainerMeetingActivity, year, month, day).show()
                                    break
                                }
//                                else
//                                {
//                                    Snackbar.make(binding.root,"Schedule a meeting first",Snackbar.LENGTH_LONG).show()
//                                }
                            }
                        }
                        if (!hasMeeting) {
//                            Snackbar.make(binding.root,"Schedule a meeting first",Snackbar.LENGTH_LONG).show()
//
                        }
                    }
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    hideLoading()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                }

            })
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        }

        binding.deleteMeeting.setOnClickListener {
            showLoading()
            var paymentTaskComplete = false
            var trainerTaskComplete = false
            val paymentDb = FirebaseDatabase.getInstance().getReference("Payment")
            paymentDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(paymentSnapshot: DataSnapshot) {
                    var paymentFound=false
                    if (paymentSnapshot.exists()) {
                        for (paymentSnap in paymentSnapshot.children) {
                            val payment = paymentSnap.getValue(PaymentData::class.java)!!
                            if (payment.trainerId == HomeActivity.auth.currentUser?.uid) {
                                Snackbar.make(binding.root, "Cannot delete meeting due to registered users for the meeting.", Snackbar.LENGTH_LONG).show()
                                paymentFound = true
                                break
                            }
                        }
                    }
                    paymentTaskComplete=true
                    if (!paymentFound) {
                        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (snap in snapshot.children) {
                                        val curr = snap.getValue(Trainer::class.java)!!
                                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
                                            if(curr.meetingDate!=null)
                                            {
                                                dbRef.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
                                                binding.dateAndTime.text = ""
                                                Snackbar.make(binding.root, "Meeting deleted successfully", Snackbar.LENGTH_LONG).show()
                                            }
                                            else
                                            {
                                                Snackbar.make(binding.root, "No Meetings Scheduled.", Snackbar.LENGTH_LONG).show()
                                            }
                                            break
                                        }
                                    }
                                }
                                trainerTaskComplete=true
                                if (paymentTaskComplete && trainerTaskComplete) {
                                    hideLoading()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                trainerTaskComplete=true
                                if (paymentTaskComplete && trainerTaskComplete) {
                                    hideLoading()
                                }
                            }
                        })
                    }
                    else
                    {
                        if (paymentTaskComplete && trainerTaskComplete) {
                            hideLoading()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            hideLoading()
        }






        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Warning")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Yes") { _, _ ->
                HomeActivity.auth.signOut()
                finish()
            }
            builder.setNegativeButton("No") { _, _ ->

            }
            builder.show()
        }

        binding.create.setOnClickListener {
            if (binding.nameInput.length() == 0 ) {
                binding.nameInput.error = "Name is required to create the meeting"
                binding.nameInput.requestFocus()
            }
            else if(binding.details.text.toString().isEmpty())
            {
                Snackbar.make(binding.root,"Cannot start meeting. No registered users currently",Snackbar.LENGTH_LONG).show()
            }
            else {

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Info")
                builder.setMessage("Are you sure you want to start the meeting?(Please start according to your meeting time only)")
                builder.setPositiveButton("Yes") { _, _ ->
                    val meetingId = getRandomMeetingID()
                    val dbRefM = FirebaseDatabase.getInstance().getReference("Meeting")
                    dbRefM.child(HomeActivity.auth.currentUser?.uid.toString()).setValue(MeetingLink(HomeActivity.auth.currentUser?.uid.toString(),meetingId))

                    startMeeting(meetingId, binding.nameInput.text.toString())
                }
                builder.setNegativeButton("No") { _, _ ->

                }
                builder.show()


            }
        }
    }

    private fun startMeeting(meetingIdInput: String, nameInput: String) {
        val userId = UUID.randomUUID().toString()

        sharedPreferences.edit().putString("name", nameInput).apply()

        val intent = Intent(this, TrainerConferenceActivity::class.java)
        intent.putExtra("meetingId", meetingIdInput)
        intent.putExtra("name", nameInput)
        intent.putExtra("userId", userId)
        startActivity(intent)



    }

    private fun getRandomMeetingID(): String {
        val id = StringBuilder()
        while (id.length != 10) {
            val rand = Random().nextInt(10)
            id.append(rand)
        }
        return id.toString()
    }

    private fun getDateTime() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        savedDay = p3
        savedMonth = p2+1
        savedYear = p1

        TimePickerDialog(this, this, hour, minute, true).show()

    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        savedHour = p1
        savedMinute = p2
        val selectedDateTime = Calendar.getInstance()
        selectedDateTime.set(savedYear, savedMonth - 1, savedDay, savedHour, savedMinute)

        val currentDateTime = Calendar.getInstance()

        if (selectedDateTime.before(currentDateTime)) {
            Snackbar.make(binding.root, "Please select a date and time after the current date and time", Snackbar.LENGTH_LONG).show()
            DatePickerDialog(this, this, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH)).show()
        } else {

            var dayStr = savedDay.toString()
            var monthStr = savedMonth.toString()
            var minuteStr = savedMinute.toString()
            var hourStr = savedHour.toString()
            if (savedDay < 10)
                dayStr = ("0$dayStr")
            if (savedMonth < 10)
                monthStr = ("0$monthStr")
            if (savedMinute < 10)
                minuteStr = ("0$minuteStr")
            if (savedHour < 10)
                hourStr = ("0$hourStr")


            binding.dateAndTime.text ="Date: ${dayStr}-${monthStr}-${savedYear}, Time:${hourStr}:${minuteStr}"

            val db = FirebaseDatabase.getInstance().getReference("Trainers")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(Trainer::class.java)!!
                            if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
//                                val dbase = FirebaseDatabase.getInstance().getReference("Trainers")
                                dbRef.child(curr.uid!!).setValue(Trainer(curr.uid!!,curr.name!!,curr.email!!,curr.fees!!,"${hourStr}:${minuteStr}","${savedYear}-${monthStr}-${dayStr}"))
                                Snackbar.make(binding.root,"New meeting scheduled successfully",Snackbar.LENGTH_LONG).show()
                                binding.dateAndTime.text ="Date: ${savedYear}-${monthStr}-${dayStr}, Time:${hourStr}:${minuteStr}"
                                startActivity(Intent(this@TrainerMeetingActivity,TrainerMeetingActivity::class.java))
                                return
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }


    private fun initLoadingDialog() {
        dialog = Dialog(this@TrainerMeetingActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showLoading() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideLoading() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    private fun collapse(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.GONE
        expand.visibility= View.VISIBLE
        collapse.visibility= View.GONE
    }

    private fun expand(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.VISIBLE
        expand.visibility= View.GONE
        collapse.visibility= View.VISIBLE
    }
}



//Duplicate Activities
//Trainer Info turn green when meeting started

































































































//package com.yogeshj.fitnessapp.trainer
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Intent
//import android.content.SharedPreferences
//import android.os.Build
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.widget.DatePicker
//import android.widget.TimePicker
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AlertDialog
//import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.yogeshj.fitnessapp.HomeActivity
//import com.yogeshj.fitnessapp.PaymentComplete
//import com.yogeshj.fitnessapp.PaymentData
//import com.yogeshj.fitnessapp.databinding.ActivityTrainerMeetingBinding
//import java.lang.StringBuilder
//import java.time.LocalDate
//import java.time.LocalTime
//import java.util.Calendar
//import java.util.Random
//import java.util.UUID
//import kotlin.math.min
//
//class TrainerMeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
//    TimePickerDialog.OnTimeSetListener {
//
//    private lateinit var binding: ActivityTrainerMeetingBinding
//    private lateinit var sharedPreferences: SharedPreferences
//
//    var day = 0
//    var month = 0
//    var year = 0
//    var hour = 0
//    var minute = 0
//
//    companion object {
//        var savedDay = 0
//        var savedMonth = 0
//        var savedYear = 0
//        var savedHour = 0
//        var savedMinute = 0
//    }
//
//    lateinit var date:LocalDate
//    lateinit var time:LocalTime
//    var flag=false
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityTrainerMeetingBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        HomeActivity.auth = FirebaseAuth.getInstance()
//
//        val currentDate = LocalDate.now()
//        val currentTime=LocalTime.now()
//
////        Log.d("currentTime","${currentTime}")
//
//
//
//
//        val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
//        dbRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (snap in snapshot.children) {
//                        val curr = snap.getValue(Trainer::class.java)!!
//                        if (HomeActivity.auth.currentUser?.uid == curr.uid) {
//                            binding.heading.text = "Welcome ${curr.name}"
//                            if(curr.meetingTime!=null)
//                            {
//                                flag=true
//                                binding.dateAndTime.text="Date: ${curr.meetingDate}, Time: ${curr.meetingTime}"
//                                date = LocalDate.parse("${curr.meetingDate}")
//                                time=LocalTime.parse("${curr.meetingTime}")
//
//                                if(currentDate.isAfter(date))
//                                {
//                                    val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
//                                    dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
//                                    binding.dateAndTime.text=""
//                                    val dbRefPay = FirebaseDatabase.getInstance().getReference("Payments")
//                                    dbRefPay.addValueEventListener(object : ValueEventListener {
//                                        override fun onDataChange(snapshot: DataSnapshot) {
//                                            if (snapshot.exists()) {
//                                                for (snap in snapshot.children) {
//                                                    val currPay = snap.getValue(PaymentData::class.java)!!
//                                                    if (HomeActivity.auth.currentUser?.uid == currPay.trainerId) {
//                                                        val dbRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
//                                                        dbRefHis.child(currPay.trainerId!!).setValue(PaymentComplete(currPay.userId,currPay.trainerId,currPay.amount,currPay.paymentStatus,currPay.meetingTime,currPay.meetingDate,currPay.email))
//                                                        snap.ref.removeValue()
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                        override fun onCancelled(error: DatabaseError) {
//
//                                        }
//
//                                    })
//                                }
//                                else if(currentDate.isEqual(date))
//                                {
//                                    var hour=time.toString().substring(0,2).toInt()
//                                    if(hour==23)
//                                        hour=0
//                                    else
//                                        hour++
//                                    if(hour<10)
//                                        time=LocalTime.parse("0$hour${time.toString().substring(2,5)}")
//                                    else
//                                        time=LocalTime.parse("$hour${time.toString().substring(2,5)}")
//                                    if(currentTime.isAfter(time))
//                                    {
//
//                                        val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
//                                        dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
//                                        binding.dateAndTime.text=""
//                                        val dbRefPay = FirebaseDatabase.getInstance().getReference("Payments")
//                                        dbRefPay.addValueEventListener(object : ValueEventListener {
//                                            override fun onDataChange(snapshot: DataSnapshot) {
//                                                if (snapshot.exists()) {
//                                                    for (snap in snapshot.children) {
//                                                        val currPay = snap.getValue(PaymentData::class.java)!!
//                                                        if (HomeActivity.auth.currentUser?.uid == currPay.trainerId) {
//                                                            val dbRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
//                                                            val node="${currPay.meetingDate}${currPay.meetingTime}"
//                                                            dbRefHis.child(node).setValue(PaymentComplete(currPay.userId,currPay.trainerId,currPay.amount,currPay.paymentStatus,currPay.meetingTime,currPay.meetingDate,currPay.email))
//                                                            snap.ref.removeValue()
//                                                        }
//                                                    }
//                                                }
//                                            }
//
//                                            override fun onCancelled(error: DatabaseError) {
//
//                                            }
//
//                                        })
//                                    }
//                                }
//
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//
//        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE)
//        binding.nameInput.setText(sharedPreferences.getString("name", ""))
//
//
//        binding.back.setOnClickListener {
//            finish()
//        }
//
//        binding.set.setOnClickListener {
//            if(binding.dateAndTime.text.isNotEmpty())
//                Snackbar.make(binding.root,"Meeting already scheduled.",Snackbar.LENGTH_LONG).show()
//            else {
//                getDateTime()
//                DatePickerDialog(this, this, year, month, day).show()
//            }
//        }
//
//        binding.update.setOnClickListener {
//            val db = FirebaseDatabase.getInstance().getReference("Trainers")
//            db.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (snap in snapshot.children) {
//                            val curr = snap.getValue(Trainer::class.java)!!
//                            //Get trainer meeting id from here
//                            if(curr.uid==HomeActivity.auth.currentUser?.uid)
//                            {
//                                if(curr.meetingDate!=null)
//                                {
//                                    getDateTime()
//                                    DatePickerDialog(this@TrainerMeetingActivity, this@TrainerMeetingActivity, year, month, day).show()
//                                }
//                                else
//                                {
//                                    Snackbar.make(binding.root,"Schedule a meeting first",Snackbar.LENGTH_LONG).show()
//                                }
//                            }
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//        }
//
//        binding.deleteMeeting.setOnClickListener {
//            val paymentDb = FirebaseDatabase.getInstance().getReference("Payments")
//            paymentDb.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(paymentSnapshot: DataSnapshot) {
//                    var paymentFound=false
//                    if (paymentSnapshot.exists()) {
//                        for (paymentSnap in paymentSnapshot.children) {
//                            val payment = paymentSnap.getValue(PaymentData::class.java)!!
//                            if (payment.trainerId == HomeActivity.auth.currentUser?.uid) {
//                                Snackbar.make(binding.root, "Cannot delete meeting due to registered users for the meeting.", Snackbar.LENGTH_LONG).show()
//                                paymentFound = true
//                                break
//                            }
//                        }
//                    }
//
//                    if (!paymentFound) {
//                        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if (snapshot.exists()) {
//                                    for (snap in snapshot.children) {
//                                        val curr = snap.getValue(Trainer::class.java)!!
//                                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
//                                            val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
//                                            dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
//                                            binding.dateAndTime.text = ""
//                                            Snackbar.make(binding.root, "Meeting deleted successfully", Snackbar.LENGTH_LONG).show()
//                                            finish()
//                                        }
//                                    }
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//
//                            }
//                        })
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//            })
//        }
//
//
//
//
//
//
//        binding.logout.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Warning")
//            builder.setMessage("Are you sure you want to logout?")
//            builder.setPositiveButton("Yes") { _, _ ->
//                HomeActivity.auth.signOut()
//                finish()
//            }
//            builder.setNegativeButton("No") { _, _ ->
//
//            }
//            builder.show()
//        }
//
//        binding.create.setOnClickListener {
//            if (binding.nameInput.length() == 0) {
//                binding.nameInput.error = "Name is required to create the meeting"
//                binding.nameInput.requestFocus()
//            } else {
//                var hour=time.toString().substring(0,2).toInt()
//                if(hour==0)
//                    hour=23
//                else
//                    hour--
//                if(hour<10)
//                    time=LocalTime.parse("0$hour${time.toString().substring(2,5)}")
//                else
//                    time=LocalTime.parse("$hour${time.toString().substring(2,5)}")
//                if(flag && currentDate.isEqual(date) && currentTime.isAfter(time)) {
//
//
//                    val meetingId = getRandomMeetingID()
//
//
//                    val dbRefM = FirebaseDatabase.getInstance().getReference("Meeting")
//                    dbRefM.child(HomeActivity.auth.currentUser?.uid.toString()).setValue(
//                        MeetingLink(
//                            HomeActivity.auth.currentUser?.uid.toString(),
//                            meetingId
//                        )
//                    )
//
//                    startMeeting(meetingId, binding.nameInput.text.toString())
//
//                }
//                else{
//                    Snackbar.make(binding.root,"Can only start 1 hour before meeting time",Snackbar.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun startMeeting(meetingIdInput: String, nameInput: String) {
//        val userId = UUID.randomUUID().toString()
//
//        sharedPreferences.edit().putString("name", nameInput).apply()
//
//        val intent = Intent(this, TrainerConferenceActivity::class.java)
//        intent.putExtra("meetingId", meetingIdInput)
//        intent.putExtra("name", nameInput)
//        intent.putExtra("userId", userId)
//        startActivity(intent)
//    }
//
//    private fun getRandomMeetingID(): String {
//        val id = StringBuilder()
//        while (id.length != 10) {
//            val rand = Random().nextInt(10)
//            id.append(rand)
//        }
//        return id.toString()
//    }
//
//    private fun getDateTime() {
//        val cal = Calendar.getInstance()
//        day = cal.get(Calendar.DAY_OF_MONTH)
//        month = cal.get(Calendar.MONTH)
//        year = cal.get(Calendar.YEAR)
//        hour = cal.get(Calendar.HOUR_OF_DAY)
//        minute = cal.get(Calendar.MINUTE)
//    }
//
//    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
//        savedDay = p3
//        savedMonth = p2+1
//        savedYear = p1
//
//        TimePickerDialog(this, this, hour, minute, true).show()
//
//    }
//
//    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
//        savedHour = p1
//        savedMinute = p2
//        val selectedDateTime = Calendar.getInstance()
//        selectedDateTime.set(savedYear, savedMonth - 1, savedDay, savedHour, savedMinute)
//
//        val currentDateTime = Calendar.getInstance()
//
//        if (selectedDateTime.before(currentDateTime)) {
//            Snackbar.make(binding.root, "Please select a date and time after the current date and time", Snackbar.LENGTH_LONG).show()
//            DatePickerDialog(this, this, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH)).show()
//        } else {
//
//            var dayStr = savedDay.toString()
//            var monthStr = savedMonth.toString()
//            var minuteStr = savedMinute.toString()
//            var hourStr = savedHour.toString()
//            if (savedDay < 10)
//                dayStr = ("0$dayStr")
//            if (savedMonth < 10)
//                monthStr = ("0$monthStr")
//            if (savedMinute < 10)
//                minuteStr = ("0$minuteStr")
//            if (savedHour < 10)
//                hourStr = ("0$hourStr")
//
//
//            binding.dateAndTime.text ="Date: ${dayStr}-${monthStr}-${savedYear}, Time:${hourStr}:${minuteStr}"
//
//            val db = FirebaseDatabase.getInstance().getReference("Trainers")
//            db.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (snap in snapshot.children) {
//                            val curr = snap.getValue(Trainer::class.java)!!
//                            if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
//                                val dbase = FirebaseDatabase.getInstance().getReference("Trainers")
//                                dbase.child(curr.uid!!).setValue(Trainer(curr.uid!!,curr.name!!,curr.email!!,curr.fees!!,"${hourStr}:${minuteStr}","${savedYear}-${monthStr}-${dayStr}"))
//                                Snackbar.make(binding.root,"New meeting scheduled successfully",Snackbar.LENGTH_LONG).show()
//                                binding.dateAndTime.text ="Date: ${savedYear}-${monthStr}-${dayStr}, Time:${hourStr}:${minuteStr}"
//                                finish()
//                            }
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//            })
//        }
//    }
//}