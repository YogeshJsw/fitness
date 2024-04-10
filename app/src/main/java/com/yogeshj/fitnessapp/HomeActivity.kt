package com.yogeshj.fitnessapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.yogeshj.fitnessapp.databinding.ActivityHomeBinding
import com.yogeshj.fitnessapp.trainer.LoginActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    private lateinit var myAdapter: PopularAdapter
    private lateinit var dataList: ArrayList<Fitness>


    companion object{
        lateinit var auth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        binding.categoryImg1.setOnClickListener {
            val intent = Intent(this@HomeActivity, CategoryActivity::class.java)
            startActivity(intent)
        }




        binding.categoryImg2.setOnClickListener {
            startActivity(Intent(this@HomeActivity,DietActivity::class.java))
        }


        setRecyclerView()

        binding.search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        //Menu bar
        binding.menu.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.bottom_sheet)
            dialog.show()
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setRecyclerView() {
        dataList = ArrayList()
        binding.recyclerPopular.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val db = Room.databaseBuilder(this@HomeActivity, AppDatabase::class.java, "FitnessDb")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("fitness.db")
            .build()
        val daoObject = db.getDao()
        val exercises = daoObject.getAll()
        for (i in exercises.indices) {
            if (exercises[i].category.contains("Popular")) {
                dataList.add(exercises[i])
            }
            myAdapter = PopularAdapter(dataList, this@HomeActivity)
            binding.recyclerPopular.adapter = myAdapter
        }
    }

    fun trainerLogin(view: View) {
        startActivity(Intent(this@HomeActivity,LoginActivity::class.java))
    }
    fun trainerRegister(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("yogesh798258@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Register as Trainer")
        intent.putExtra(Intent.EXTRA_TEXT, "Dear Sir,\n\nI am interested in registering as a trainer in your fitness application. Please find attached the following documents for your review:\n\n1. Valid identification (e.g., Aadhar Card, PAN Card, etc.)\n2. Copies of relevant certificates or qualifications.3.A passport size photograph\n\nIf there are any additional forms or information required, please let me know.\n\nThank you for considering my application.\n\nBest regards,\n[Your Name]\n[Your Contact Information]")

        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    fun userLogin(view: View) {
        startActivity(Intent(this@HomeActivity,com.yogeshj.fitnessapp.user.LoginActivity::class.java))
    }
}