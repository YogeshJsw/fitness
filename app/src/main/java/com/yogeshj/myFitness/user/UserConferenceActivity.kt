package com.yogeshj.myFitness.user

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityUserConferenceBinding
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment

class UserConferenceActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserConferenceBinding
    var meetingId=""
    var name=""
    var userId=""
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUserConferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,"Join the meeting\n Meeting Id: $meetingId")
            startActivity(Intent.createChooser(intent,"Share via"))
            hideLoading()
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
    private fun initLoadingDialog() {
        dialog = Dialog(this@UserConferenceActivity)
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