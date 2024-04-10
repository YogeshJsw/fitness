package com.yogeshj.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.yogeshj.fitnessapp.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private lateinit var myAdapter: SearchAdapter
    private lateinit var dataList: ArrayList<Fitness>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
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