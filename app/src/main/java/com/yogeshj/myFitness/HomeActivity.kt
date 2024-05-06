package com.yogeshj.myFitness

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.yogeshj.myFitness.databinding.ActivityHomeBinding
import com.yogeshj.myFitness.trainer.LoginActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    private lateinit var myAdapter: PopularAdapter
    private lateinit var dataList: ArrayList<Fitness>

    private lateinit var dialog:Dialog

    companion object{
        lateinit var auth: FirebaseAuth
    }

    override fun onResume() {
        showLoading()
        super.onResume()
        setRecyclerView()
        hideLoading()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadingDialog()

        auth= FirebaseAuth.getInstance()

        MobileAds.initialize(this) {}
        val adRequest=AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        binding.categoryImg1.setOnClickListener {
            showLoading()
            val intent = Intent(this@HomeActivity, CategoryActivity::class.java)
            startActivity(intent)
            hideLoading()

        }




        binding.categoryImg2.setOnClickListener {
            showLoading()
            startActivity(Intent(this@HomeActivity,DietActivity::class.java))
            hideLoading()
        }




        binding.search.setOnClickListener {
            showLoading()
            startActivity(Intent(this, SearchActivity::class.java))
            hideLoading()
        }

        //Menu bar
        binding.menu.setOnClickListener {
            showLoading()
            val dialogg = Dialog(this)
            dialogg.setContentView(R.layout.bottom_sheet)
            dialogg.show()
            dialogg.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialogg.window!!.setGravity(Gravity.BOTTOM)
            dialogg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            hideLoading()
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
        showLoading()

//        if(auth.currentUser!=null)
//        {
//
//            val db= FirebaseDatabase.getInstance().getReference("Trainers")
//            db.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.exists())
//                    {
//                        for (snap in snapshot.children)
//                        {
//                            val curr=snap.getValue(Trainer::class.java)!!
//                            if(auth.currentUser!!.uid==curr.uid)
//                            {
//                                startActivity(Intent(this@HomeActivity, TrainerMeetingActivity::class.java))
//                                break
//                            }
//                        }
//
//                    }
//
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//
//        }
//        else
            startActivity(Intent(this@HomeActivity,LoginActivity::class.java))
        hideLoading()


    }

    fun trainerRegisterAdmin(view: View){
        startActivity(Intent(this@HomeActivity,com.yogeshj.myFitness.admin.LoginActivity::class.java))
    }

    fun trainerRegister(view: View) {
        showLoading()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("yogesh798258@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Register as Trainer")
        intent.putExtra(Intent.EXTRA_TEXT, "Respected,\n\nI am interested in registering as a trainer in your fitness application. Please find attached the following documents for your review:\n\n1. Valid identification (e.g., Aadhar Card, PAN Card, etc.)\n2. Copies of relevant certificates or qualifications.\n3.A passport size photograph\n\nIf there are any additional forms or information required, please let me know.\n\nThank you for considering my application.\n\nBest regards,\n[Your Name]\n[Your Contact Information]")

        startActivity(Intent.createChooser(intent, "Send Email"))
        hideLoading()
    }

    fun userLogin(view: View) {
        startActivity(Intent(this@HomeActivity,com.yogeshj.myFitness.user.LoginActivity::class.java))
    }
    private fun initLoadingDialog() {
        dialog = Dialog(this@HomeActivity)
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