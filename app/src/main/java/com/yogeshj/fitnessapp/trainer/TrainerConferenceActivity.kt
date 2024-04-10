package com.yogeshj.fitnessapp.trainer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogeshj.fitnessapp.R
import com.yogeshj.fitnessapp.databinding.ActivityTrainerConferenceBinding
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment
import com.zegocloud.uikit.prebuilt.videoconference.config.ZegoLeaveConfirmDialogInfo


class TrainerConferenceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityTrainerConferenceBinding
    var meetingId=""
    var name=""
    var userId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTrainerConferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        meetingId= intent.getStringExtra("meetingId")!!
        name=intent.getStringExtra("name")!!
        userId=intent.getStringExtra("userId")!!

        binding.meetingIdTextView.text="Meeting Id: $meetingId"

        addFragment()




        binding.shareBtn.setOnClickListener {
            val intent= Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT,"Join the meeting\n Meeting Id: $meetingId")
            startActivity(Intent.createChooser(intent,"Share via"))
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

}