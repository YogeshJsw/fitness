package com.yogeshj.myFitness.admin

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = HomeActivity.auth.currentUser
        initLoadingDialog()

        MobileAds.initialize(this) {}
        val adRequest= AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.btnLogin.setOnClickListener {
            showLoading()
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                hideLoading()
                Snackbar.make(
                    binding.emailLogin,
                    "Email or password cannot be empty.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if(email == "admin@gmail.com" && password=="yogesh")
            {
                startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
                finish()
                hideLoading()
            }
            else
            {
                hideLoading()
                Snackbar.make(binding.root,"Incorrect email or password entered.",Snackbar.LENGTH_LONG).show()

            }

        }

    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@LoginActivity)
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