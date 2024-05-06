package com.yogeshj.myFitness

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityDietActivityBinding

class DietActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDietActivityBinding
    private lateinit var dialog:Dialog

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDietActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()


        MobileAds.initialize(this@DietActivity) {}
        val adRequest=AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(this@DietActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })


        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@DietActivity)
            }
            finish()
        }

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

        binding.expand1.setOnClickListener {
            showLoading()
            expand(binding.details1,binding.expand1,binding.collapse1)
            hideLoading()
        }

        binding.collapse1.setOnClickListener {
            showLoading()
            collapse(binding.details1,binding.expand1,binding.collapse1)
            hideLoading()
        }

        binding.expand2.setOnClickListener {
            showLoading()
            expand(binding.details2,binding.expand2,binding.collapse2)
            hideLoading()
        }

        binding.collapse2.setOnClickListener {
            showLoading()
            collapse(binding.details2,binding.expand2,binding.collapse2)
            hideLoading()
        }

        binding.expand3.setOnClickListener {
            showLoading()
            expand(binding.details3,binding.expand3,binding.collapse3)
            hideLoading()
        }

        binding.collapse3.setOnClickListener {
            showLoading()
            collapse(binding.details3,binding.expand3,binding.collapse3)
            hideLoading()
        }

        binding.expand4.setOnClickListener {
            showLoading()
            expand(binding.details4,binding.expand4,binding.collapse4)
            hideLoading()
        }

        binding.collapse4.setOnClickListener {
            showLoading()
            collapse(binding.details4,binding.expand4,binding.collapse4)
            hideLoading()
        }
    }

    private fun collapse(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.GONE
        expand.visibility=View.VISIBLE
        collapse.visibility=View.GONE
    }

    private fun expand(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.VISIBLE
        expand.visibility=View.GONE
        collapse.visibility=View.VISIBLE
    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@DietActivity  )
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