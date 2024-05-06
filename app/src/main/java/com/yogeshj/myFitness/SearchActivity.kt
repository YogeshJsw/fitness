package com.yogeshj.myFitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yogeshj.myFitness.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private lateinit var myAdapter: SearchAdapter
    private lateinit var dataList: ArrayList<Fitness>

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this@SearchActivity) {}
        val adRequest=AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(this@SearchActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })



        binding.search.requestFocus()
        val db = Room.databaseBuilder(this@SearchActivity, AppDatabase::class.java, "FitnessDb")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("fitness.db")
            .build()
        val daoObject = db.getDao()
        val exercises = daoObject.getAll()
        setRecyclerView(exercises)


        //Search Implementation
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != "") {
                    filterData(p0.toString(), exercises)
                } else {
                    setRecyclerView(exercises)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })


        //Back Button
        binding.backImg.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@SearchActivity)
            }
            finish()
        }
    }

    private fun filterData(filterText: String, exercises: List<Fitness?>) {
        val filterData = ArrayList<Fitness>()
        for (i in exercises.indices) {
            if (exercises[i]!!.name.lowercase().contains(filterText.lowercase())) {
                filterData.add(exercises[i]!!)
            }
            myAdapter.filterList(filterList = filterData)
        }
    }

    private fun setRecyclerView(exercises: List<Fitness?>) {
        dataList = ArrayList()
        binding.recycler.layoutManager = LinearLayoutManager(this)


        for (i in exercises.indices) {
            if (exercises[i]!!.category.contains("Popular")) {
                dataList.add(exercises[i]!!)
            }
            myAdapter = SearchAdapter(dataList, this@SearchActivity)
            binding.recycler.adapter = myAdapter
        }
    }

}