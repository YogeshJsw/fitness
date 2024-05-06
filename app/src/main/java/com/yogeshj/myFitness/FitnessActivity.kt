package com.yogeshj.myFitness

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.Window
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityFitnessBinding


class FitnessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFitnessBinding
    private lateinit var dialog:Dialog

    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()

        MobileAds.initialize(this@FitnessActivity) {}
        val adRequest=AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(this@FitnessActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })

        //back button
        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@FitnessActivity)
            }
            finish()
        }



        //heading
        showLoading()
        val title = intent.getStringExtra("title")
        binding.heading.text = title


        //About data
        val about = intent.getStringExtra("about")
        val lines = about?.split("\n")
        var formattedAbout = """
            <html>
                <body>
                    <p style="text-align: left; line-height: 1.5;">
                        
                        ${if (lines?.firstOrNull() == title) "<b>${lines?.first()}</b>" else lines?.first()}<br/>
                    </p>
                    <ul style="text-align: left; padding-left: 20px;">
                    """.trimIndent()

        if (lines != null) {
            for (i in 1 until lines.size) {
                formattedAbout += if (lines[i] == "Benefits:")
                    "<b>${lines[i]}</b>"
                else
                    "<li>${lines[i]}</li>"
            }
        }

        formattedAbout += """
                    </ul>
                </body>
            </html>
        """.trimIndent()

        binding.aboutText.text = Html.fromHtml(formattedAbout, Html.FROM_HTML_MODE_COMPACT)


        //Steps Data
        val steps = intent.getStringExtra("steps")
        val stepsLines = steps?.split("\n")

        var formattedSteps = """
            <html>
                <body>
                    <p style="text-align: left; line-height: 1.5;">
                    """.trimIndent()

        if (stepsLines != null) {
            for ((index, step) in stepsLines.withIndex()) {
                formattedSteps += "<b>Step${index + 1}:</b> $step<br/>"
            }
        }

        formattedSteps += """
                    </p>
                </body>
            </html>
        """.trimIndent()

        binding.stepsText.text = Html.fromHtml(formattedSteps, Html.FROM_HTML_MODE_COMPACT)
        Log.d("Steps Data", formattedSteps)

        //Video player
        val videoId = intent.getStringExtra("link")
        val str =
            "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/${videoId}?si=IuHVwo1lER8_pCCW&showinfo=0\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture;\"></iframe>"

        val webView = binding.webView
        webView.loadData(str, "text/html", "utf-8")
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()


        binding.stepsBtn.setTextColor(Color.BLACK)
        hideLoading()

        //Button Fragments

        binding.stepsBtn.setOnClickListener {
            showLoading()
            binding.stepsBtn.setTextColor(Color.WHITE)
            binding.stepsScroll.visibility = View.VISIBLE
            binding.aboutScroll.visibility = View.GONE
            binding.aboutBtn.setTextColor(Color.BLACK)
            hideLoading()
        }
        binding.aboutBtn.setOnClickListener {
            showLoading()
            binding.aboutBtn.setTextColor(Color.WHITE)
            binding.aboutScroll.visibility = View.VISIBLE
            binding.stepsScroll.visibility = View.GONE
            binding.stepsBtn.setTextColor(Color.BLACK)
            hideLoading()
        }
    }
    private fun initLoadingDialog() {
        dialog = Dialog(this@FitnessActivity)
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