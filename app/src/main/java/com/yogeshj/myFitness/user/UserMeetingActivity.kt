package com.yogeshj.myFitness.user

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.PaymentData
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityUserMeetingBinding
import com.yogeshj.myFitness.trainer.MeetingLink
import com.yogeshj.myFitness.trainer.Trainer
import java.util.UUID

class UserMeetingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserMeetingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dialog:Dialog

    private var mInterstitialAd: InterstitialAd? = null

    override fun onPause() {
        super.onPause()
        binding.idDisplay.text="Fitness"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()
        showLoading()
        HomeActivity.auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this@UserMeetingActivity) {}
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this@UserMeetingActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })

        sharedPreferences = getSharedPreferences("name_pref", MODE_PRIVATE)
        binding.nameInput.setText(sharedPreferences.getString("name", ""))


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
                hideLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                hideLoading()
            }

        })

        hideLoading()
        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@UserMeetingActivity)
            }
            finish()
        }

        //Get meeting id in id_display
        binding.meetingId.setOnClickListener {
            showLoading()
            val db = FirebaseDatabase.getInstance().getReference("Payment")



            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(PaymentData::class.java)!!
                            if(curr.userId==HomeActivity.auth.currentUser?.uid.toString())
                            {

                                val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
                                dbRef.addValueEventListener(object : ValueEventListener {
//                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (snap in snapshot.children) {
                                                val curr2 = snap.getValue(Trainer::class.java)!!
                                                if (curr.trainerId == curr2.uid) {
                                                    if(curr2.meetingTime!=null)
                                                    {
//                                                        val meetingDateTime = LocalDateTime.parse("${curr2.meetingDate}T${curr2.meetingTime}")

//                                                        val currentTime = LocalDateTime.now()
//                                                        val oneHourLater = currentTime.plusHours(-1)

//                                                        if (meetingDateTime.isAfter(oneHourLater)) {
                                                            getMeetingId(curr.trainerId)
                                                            break
//                                                        }
//                                                        else {
//                                                            val remainingTime = Duration.between(currentTime, meetingDateTime)
//                                                            val minutesLeft = remainingTime.toMinutes()
//                                                            Snackbar.make(binding.root, "Meeting will start in $minutesLeft minutes", Snackbar.LENGTH_LONG).show()
//                                                            break
//                                                        }
                                                    }

                                                }
                                            }
                                        }
                                        hideLoading()

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        hideLoading()
                                    }

                                })
                            }
                        }
                    }
                    hideLoading()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                }

            })
        }


        binding.create.setOnClickListener {
            val meetingId = binding.meetingIdInput.text.toString()
//            Toast.makeText(this@UserMeetingActivity,"$meetingId , ${binding.idDisplay.text.toString()}",Toast.LENGTH_LONG).show()
            if (meetingId.length != 10 || (meetingId.length==10 && meetingId!=binding.idDisplay.text.toString())) {
                binding.meetingIdInput.error = "Invalid Meeting Id"
                binding.meetingIdInput.requestFocus()

            } else {
                if (binding.nameInput.length() == 0) {
                    binding.nameInput.error = "Name is required to join the meeting"
                    binding.nameInput.requestFocus()
                } else {
                    showLoading()
                    startMeeting(binding.meetingIdInput.text.toString(),binding.nameInput.text.toString())
                    hideLoading()
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
                            binding.idDisplay.text=curr.meetingLink.toString()
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

    private fun initLoadingDialog() {
        dialog = Dialog(this@UserMeetingActivity)
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
}

//Upload images for trainers
//After creating meeting save it so that same meeting id exists
//Create new meeting snackbar (1 hour before)
//App icon
//Delete passed meetings

//Don't allow users to start the meeting if incorrect meeting id is entered

//Payment history option for each user
//Notes for users by trainer