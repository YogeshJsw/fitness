package com.yogeshj.myFitness.trainer

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.PaymentComplete
import com.yogeshj.myFitness.PaymentData
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityTrainerConferenceBinding
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment
import com.zegocloud.uikit.prebuilt.videoconference.config.ZegoLeaveConfirmDialogInfo


class TrainerConferenceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityTrainerConferenceBinding
    var meetingId=""
    var name=""
    var userId=""
    private lateinit var dialog: Dialog

    private lateinit var dbRefMeeting:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityTrainerConferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HomeActivity.auth=FirebaseAuth.getInstance()


        initLoadingDialog()

        showLoading()

        meetingId= intent.getStringExtra("meetingId")!!
        name=intent.getStringExtra("name")!!
        userId=intent.getStringExtra("userId")!!

        binding.meetingIdTextView.text="Meeting Id: $meetingId"

        addFragment()


        hideLoading()


        binding.shareBtn.setOnClickListener {

            showLoading()
            val intent= Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT,"Join the meeting\n Meeting Id: $meetingId")
            startActivity(Intent.createChooser(intent,"Share via"))
            hideLoading()
        }
    }

    private fun addFragment() {
        val appID: Long = TrainerAppConstants.appId
        val appSign: String = TrainerAppConstants.appSign

        val config = ZegoUIKitPrebuiltVideoConferenceConfig()

        val leaveConfirmDialogInfo = ZegoLeaveConfirmDialogInfo()
        leaveConfirmDialogInfo.title = "Leave the conference";
        leaveConfirmDialogInfo.message = "Are you sure to end the conference?";
        leaveConfirmDialogInfo.cancelButtonName = "Cancel";
        leaveConfirmDialogInfo.confirmButtonName = "Confirm";
        config.leaveConfirmDialogInfo = leaveConfirmDialogInfo;


        //Disable camera
//        config.turnOnCameraWhenJoining = false;
//        config.audioVideoViewConfig.showCameraStateOnView = false;
//        config.topMenuBarConfig.buttons = listOf(
//            ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON,
//        );
//        config.bottomMenuBarConfig.buttons = listOf(
//            ZegoMenuBarButtonName.LEAVE_BUTTON,
//            ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON,
//            ZegoMenuBarButtonName.CHAT_BUTTON
//        );


        val fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
            appID,
            appSign,
            userId,
            name,
            meetingId,
            config
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commitNow()


    }

    override fun onStop() {
        super.onStop()
//        initLoadingDialog()
//        showLoading()

        dbRefMeeting = FirebaseDatabase.getInstance().getReference("Meeting")
        dbRefMeeting.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(MeetingLink::class.java)

                        if (HomeActivity.auth.currentUser?.uid == curr!!.trainerId) {
                            // Update the trainer data
                            snap.ref.removeValue()
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, if needed
                Log.d("Error in deleting meeting",error.message)
            }
        })

        val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(Trainer::class.java)

                        if (HomeActivity.auth.currentUser?.uid == curr!!.uid) {
                            // Update the trainer data
                            dbRef.child(curr.uid!!).setValue(Trainer(curr.uid!!,curr.name!!,curr.email!!,curr.fees!!))
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, if needed
            }
        })
        val dbRefPay = FirebaseDatabase.getInstance().getReference("Payment")
        dbRefPay.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val currPay = snap.getValue(PaymentData::class.java)!!
                        if (HomeActivity.auth.currentUser?.uid == currPay.trainerId) {
                            val dbRefHis = FirebaseDatabase.getInstance().getReference("PaymentHis")
                            dbRefHis.child(snap.key!!).setValue(PaymentComplete(currPay.userId,currPay.trainerId,currPay.amount,currPay.paymentStatus,currPay.meetingTime,currPay.meetingDate,currPay.email))

                            snap.ref.removeValue()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
//        hideLoading()
























//        val dbRefMeeting = FirebaseDatabase.getInstance().getReference("Meeting")
//        val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")

//        var meetingTaskComplete = false
//        var trainersTaskComplete = false

//        dbRefMeeting.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (snap in snapshot.children) {
//                        val curr = snap.getValue(MeetingLink::class.java)
//                        if (curr != null && HomeActivity.auth.currentUser?.uid == curr.trainerId) {
//                            snap.ref.removeValue()
//                            break
//                        }
//                    }
//                }
//                meetingTaskComplete = true
//                if (meetingTaskComplete && trainersTaskComplete) {
//                    hideLoading()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                meetingTaskComplete = true
//                if (meetingTaskComplete && trainersTaskComplete) {
//                    hideLoading()
//                }
//            }
//        })
//
//        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (snap in snapshot.children) {
//                        val curr = snap.getValue(Trainer::class.java)
//                        if (curr != null && HomeActivity.auth.currentUser?.uid == curr.uid) {
//                            dbRef.child(curr.uid!!).setValue(
//                                Trainer(curr.uid!!, curr.name!!, curr.email!!, curr.fees!!)
//                            )
//                        }
//                    }
//                }
//                trainersTaskComplete = true
//                if (meetingTaskComplete && trainersTaskComplete) {
//                    hideLoading()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                trainersTaskComplete = true
//                if (meetingTaskComplete && trainersTaskComplete) {
//                    hideLoading()
//                }
//            }
//        })
    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@TrainerConferenceActivity)
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