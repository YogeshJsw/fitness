package com.yogeshj.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.yogeshj.fitnessapp.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding

    private lateinit var myAdapter: CategoryAdapter
    private lateinit var dataList: ArrayList<Fitness>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("title")
        binding.heading.text = title
        setRecyclerView()

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
}