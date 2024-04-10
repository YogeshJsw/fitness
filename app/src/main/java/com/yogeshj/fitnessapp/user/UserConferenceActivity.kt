package com.yogeshj.fitnessapp.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yogeshj.fitnessapp.R
import com.yogeshj.fitnessapp.databinding.ActivityUserConferenceBinding
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment

class UserConferenceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserConferenceBinding
    var meetingId=""
    var name=""
    var userId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUserConferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        meetingId= intent.getStringExtra("meetingId")!!
        name=intent.getStringExtra("name")!!
        userId=intent.getStringExtra("userId")!!

        binding.meetingIdTextView.text="Meeting Id: $meetingId"

        addFragment()



        binding.shareBtn.setOnClickListener {
            val intent= Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,"Join the meeting\n Meeting Id: $meetingId")
            startActivity(Intent.createChooser(intent,"Share via"))
        }
    }

    private fun addFragment() {
        val appID: Long = UserAppConstants.appId
        val appSign: String = UserAppConstants.appSign

        val config = ZegoUIKitPrebuiltVideoConferenceConfig()

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