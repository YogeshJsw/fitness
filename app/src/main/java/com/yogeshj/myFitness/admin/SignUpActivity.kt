package com.yogeshj.myFitness.admin

import android.app.Dialog
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivitySignUp2Binding
import com.yogeshj.myFitness.trainer.Trainer

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUp2Binding

    private lateinit var dbRef: DatabaseReference
    private lateinit var dialog: Dialog

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()

        MobileAds.initialize(this@SignUpActivity) {}
        val adRequest=AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(this@SignUpActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })

        binding.btnSignUp.setOnClickListener {
            showLoading()
            val name = binding.nameRegister.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()
            val fees = binding.fees.text.toString().toInt()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || fees == 0) {
                hideLoading()
                Snackbar.make(
                    binding.root,
                    "Please fill up all the fields.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            HomeActivity.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        addUserToDatabase(name, email, HomeActivity.auth.currentUser!!.uid, fees)
                        Snackbar.make(
                            binding.root,
                            "Account Creation successful",
                            Snackbar.LENGTH_LONG
                        ).show()
                        hideLoading()
                    }
                }.addOnFailureListener { err ->
                    hideLoading()
                    Snackbar.make(binding.root, "Error: ${err.message}", Snackbar.LENGTH_LONG)
                        .show()
                }
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@SignUpActivity)
            }
        }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String, fees: Int) {

        dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
        dbRef.child(uid).setValue(Trainer(uid, name, email, fees))
    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@SignUpActivity)
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