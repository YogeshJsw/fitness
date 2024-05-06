package com.yogeshj.myFitness

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityCategoryBinding


class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding

    private lateinit var myAdapter: CategoryAdapter
    private lateinit var dataList: ArrayList<Fitness>
    private lateinit var dialog: Dialog

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        MobileAds.initialize(this@CategoryActivity) {}


        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(this@CategoryActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("Category", adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("Category", "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        initLoadingDialog()

        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@CategoryActivity)
            }
            else {
                Log.d("Category", "The interstitial ad wasn't ready yet.")
            }
            finish()
        }

        showLoading()
        val title = intent.getStringExtra("title")
        binding.heading.text = title
        setRecyclerView()
        hideLoading()
    }

    private fun setRecyclerView() {
        dataList = ArrayList()
        binding.recycler.layoutManager = LinearLayoutManager(this)

        val db = Room.databaseBuilder(this@CategoryActivity, AppDatabase::class.java, "FitnessDb")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("fitness.db")
            .build()

        val daoObject = db.getDao()
        val exercises = daoObject.getAll()
        for (i in exercises.indices) {
            dataList.add(exercises[i])
            myAdapter = CategoryAdapter(dataList, this@CategoryActivity)
            binding.recycler.adapter = myAdapter
        }

    }
    private fun initLoadingDialog() {
        dialog = Dialog(this@CategoryActivity)
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