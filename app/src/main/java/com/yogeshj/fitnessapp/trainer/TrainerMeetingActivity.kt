package com.yogeshj.fitnessapp.trainer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.PaymentComplete
import com.yogeshj.fitnessapp.PaymentData
import com.yogeshj.fitnessapp.databinding.ActivityTrainerMeetingBinding
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Random
import java.util.UUID
import kotlin.math.min

class TrainerMeetingActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivityTrainerMeetingBinding
    private lateinit var sharedPreferences: SharedPreferences

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainerMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentDate = LocalDate.now()
        val currentTime=LocalTime.now()

//        Log.d("currentTime","${currentTime}")




        val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(Trainer::class.java)!!
                        if (HomeActivity.auth.currentUser?.uid == curr.uid) {
                            binding.heading.text = "Welcome ${curr.name}"
                            if(curr.meetingTime!=null)
                            {
                                binding.dateAndTime.text="Date: ${curr.meetingDate}, Time: ${curr.meetingTime}"
                                date = LocalDate.parse("${curr.meetingDate}")
                                time=LocalTime.parse("${curr.meetingTime}")

                                if(currentDate.isAfter(date))
                                {
                                    val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
                                    dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
                                    binding.dateAndTime.text=""
                                    val dbRefPay = FirebaseDatabase.getInstance().getReference("Payments")
                                    dbRefPay.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                for (snap in snapshot.children) {
                                                    val currPay = snap.getValue(PaymentData::class.java)!!
                                                    if (HomeActivity.auth.currentUser?.uid == currPay.trainerId) {
                                                        val dbRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
                                                        dbRefHis.child(currPay.trainerId!!).setValue(PaymentComplete(currPay.userId,currPay.trainerId,currPay.amount,currPay.paymentStatus,currPay.meetingTime,currPay.meetingDate,currPay.email))
                                                        snap.ref.removeValue()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })
                                }
                                else if(currentDate.isEqual(date))
                                {
                                    var hour=time.toString().substring(0,2).toInt()
                                    if(hour==23)
                                        hour=0
                                    else
                                        hour++
                                    if(hour<10)
                                        time=LocalTime.parse("0$hour${time.toString().substring(2,5)}")
                                    else
                                        time=LocalTime.parse("$hour${time.toString().substring(2,5)}")
                                    if(currentTime.isAfter(time))
                                    {

                                        val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
                                        dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
                                        binding.dateAndTime.text=""
                                        val dbRefPay = FirebaseDatabase.getInstance().getReference("Payments")
                                        dbRefPay.addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    for (snap in snapshot.children) {
                                                        val currPay = snap.getValue(PaymentData::class.java)!!
                                                        if (HomeActivity.auth.currentUser?.uid == currPay.trainerId) {
                                                            val dbRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
                                                            val node="${currPay.meetingDate}${currPay.meetingTime}"
                                                            dbRefHis.child(node).setValue(PaymentComplete(currPay.userId,currPay.trainerId,currPay.amount,currPay.paymentStatus,currPay.meetingTime,currPay.meetingDate,currPay.email))
                                                            snap.ref.removeValue()
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }

                                        })
                                    }
                                }

                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE)
        binding.nameInput.setText(sharedPreferences.getString("name", ""))


        binding.back.setOnClickListener {
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

        binding.deleteMeeting.setOnClickListener {
            val paymentDb = FirebaseDatabase.getInstance().getReference("Payments")
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

                    if (!paymentFound) {
                        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (snap in snapshot.children) {
                                        val curr = snap.getValue(Trainer::class.java)!!
                                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
                                            val dbReff = FirebaseDatabase.getInstance().getReference("Trainers")
                                            dbReff.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!))
                                            binding.dateAndTime.text = ""
                                            Snackbar.make(binding.root, "Meeting deleted successfully", Snackbar.LENGTH_LONG).show()
                                            finish()
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
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
            if (binding.nameInput.length() == 0) {
                binding.nameInput.error = "Name is required to create the meeting"
                binding.nameInput.requestFocus()
            } else {
                val meetingId = getRandomMeetingID()


                val dbRefM = FirebaseDatabase.getInstance().getReference("Meeting")
                dbRefM.child(HomeActivity.auth.currentUser?.uid.toString()).setValue(MeetingLink(HomeActivity.auth.currentUser?.uid.toString(), meetingId))

                startMeeting(meetingId, binding.nameInput.text.toString())


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
        hour = cal.get(Calendar.HOUR)
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
        var dayStr= savedDay.toString()
        var monthStr= savedMonth.toString()
        var minuteStr= savedMinute.toString()
        var hourStr= savedHour.toString()
        if(savedDay<10)
            dayStr=("0$dayStr")
        if(savedMonth<10)
            monthStr=("0$monthStr")
        if(savedMinute<10)
            minuteStr =("0$minuteStr")
        if(savedHour<10)
            hourStr=("0$hourStr")


        binding.dateAndTime.text = "Date: ${dayStr}-${monthStr}-${savedYear}, Time:${hourStr}:${minuteStr}"

        val db = FirebaseDatabase.getInstance().getReference("Trainers")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(Trainer::class.java)!!
                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
                            val dbase = FirebaseDatabase.getInstance().getReference("Trainers")
                            dbase.child(curr.uid!!).setValue(Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!, "${hourStr}:${minuteStr}", "${savedYear}-${monthStr}-${dayStr}"))
                            Snackbar.make(binding.root,"New meeting scheduled successfully",Snackbar.LENGTH_LONG).show()
                            binding.dateAndTime.text = "Date: ${savedYear}-${monthStr}-${dayStr}, Time:${hourStr}:${minuteStr}"
                            finish()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}