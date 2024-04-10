package com.yogeshj.fitnessapp.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.PaymentData
import com.yogeshj.fitnessapp.R
import com.yogeshj.fitnessapp.databinding.ActivityUserMeetingBinding
import com.yogeshj.fitnessapp.trainer.MeetingLink
import com.yogeshj.fitnessapp.trainer.Trainer
import com.yogeshj.fitnessapp.trainer.TrainerConferenceActivity
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.util.Random
import java.util.UUID

class UserMeetingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMeetingBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        val db = FirebaseDatabase.getInstance().getReference("Users")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(User::class.java)!!
                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
                            binding.heading.text = "Welcome ${curr.name}"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        //Get meeting id in id_display
        binding.meetingId.setOnClickListener {
            val db = FirebaseDatabase.getInstance().getReference("Payments")



            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(PaymentData::class.java)!!
                            if(curr.userId==HomeActivity.auth.currentUser?.uid.toString())
                            {

                                val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
                                dbRef.addValueEventListener(object : ValueEventListener {
                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (snap in snapshot.children) {
                                                val curr2 = snap.getValue(Trainer::class.java)!!
                                                if (curr.trainerId == curr2.uid) {
                                                    if(curr2.meetingTime!=null)
                                                    {
                                                        var date = LocalDate.parse("${curr2.meetingDate}")
                                                        var time= LocalTime.parse("${curr2.meetingTime}")

                                                        val currentDate = LocalDate.now()
                                                        val currentTime=LocalTime.now()
                                                        if(currentDate.isEqual(date) && (currentTime.isAfter(time)) )
                                                        {
                                                            getMeetingId(curr.trainerId)
                                                        }
                                                        else
                                                        {
                                                            Snackbar.make(binding.root,"Please wait for the meeting to start",Snackbar.LENGTH_LONG).show()
                                                        }
                                                    }
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

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }







        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE)
        binding.nameInput.setText(sharedPreferences.getString("name", ""))

        binding.create.setOnClickListener {
            val meetingId = binding.meetingIdInput.text.toString()
            if (meetingId.length != 10) {
                binding.meetingIdInput.error = "Invalid Meeting Id"
                binding.meetingIdInput.requestFocus()

            } else {
                if (binding.nameInput.length() == 0) {
                    binding.nameInput.error = "Name is required to join the meeting"
                    binding.nameInput.requestFocus()
                } else {
                    startMeeting(binding.meetingIdInput.text.toString(),binding.nameInput.text.toString())
                }
            }
        }
    }

    private fun getMeetingId(trainerId: String?) {
        val db = FirebaseDatabase.getInstance().getReference("Meeting")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(MeetingLink::class.java)!!
                        //Get trainer meeting id from here
                        if(curr.trainerId==trainerId)
                        {
                            binding.idDisplay.text=curr.meetingLink
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun startMeeting(meetingIdInput: String, nameInput: String) {
        val userId = UUID.randomUUID().toString()

        sharedPreferences.edit().putString("name", nameInput).apply()

        val intent = Intent(this, UserConferenceActivity::class.java)
        intent.putExtra("meetingId", meetingIdInput)
        intent.putExtra("name", nameInput)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}

//Upload images for trainers
//Update meeting time option to trainers
//Loading bar in each page

