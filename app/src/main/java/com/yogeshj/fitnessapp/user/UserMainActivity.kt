package com.yogeshj.fitnessapp.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.PaymentResultWithDataListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.PaymentData
import com.yogeshj.fitnessapp.databinding.ActivityUserMainBinding
import com.yogeshj.fitnessapp.trainer.Trainer


class UserMainActivity : AppCompatActivity(), PaymentResultWithDataListener {
    private lateinit var binding: ActivityUserMainBinding

    private lateinit var trainerNames: ArrayList<Trainer>
    private lateinit var adapter: UserAdapter
    private lateinit var dbRef: DatabaseReference

    companion object {
        var id: String? = null
        var amount: Int = 0
        var meetingTime:String?=null
        var meetingDate:String?=null
        var meetingLink:String?=null
//        var paymentTo:String?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinMeeting.setOnClickListener {
            val intent = Intent(this@UserMainActivity, UserMeetingActivity::class.java)
            startActivity(intent)

        }

        binding.back.setOnClickListener {
            finish()
        }

        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Warning")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Yes"){_,_->
                HomeActivity.auth.signOut()
                finish()
            }
            builder.setNegativeButton("No"){_,_->

            }
            builder.show()
        }


        //Get current user name
        val db = FirebaseDatabase.getInstance().getReference("Users")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(User::class.java)!!
                        if (HomeActivity.auth.currentUser!!.uid == curr.uid) {
                            binding.heading.text = "Welcome ${curr.name}"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        //Get all trainers
        dbRef = FirebaseDatabase.getInstance().getReference("Trainers")

        trainerNames = ArrayList()
        adapter = UserAdapter(this, trainerNames)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        //Reading data from database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trainerNames.clear()
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val currTrainer = snap.getValue(Trainer::class.java)!!
                        trainerNames.add(currTrainer)
                    }
                    adapter = UserAdapter(this@UserMainActivity, trainerNames)
                    binding.recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    override fun onPaymentSuccess(p0: String?, p1: com.razorpay.PaymentData?) {
        val userId = HomeActivity.auth.currentUser?.uid
        val paymentRef = FirebaseDatabase.getInstance().getReference("Payments")
        val paymentId = paymentRef.push().key // unique payment ID
        val paymentData = PaymentData(userId, id, amount, "success", meetingTime, meetingDate,HomeActivity.auth.currentUser?.email)
        if (paymentId != null) {
            paymentRef.child(paymentId).setValue(paymentData)
                .addOnSuccessListener {
                    Toast.makeText(this@UserMainActivity, "Payment Success", Toast.LENGTH_LONG)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@UserMainActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
        }
    }



    override fun onPaymentError(p0: Int, p1: String?, p2: com.razorpay.PaymentData?) {
        Toast.makeText(this@UserMainActivity, "Error: $p1", Toast.LENGTH_LONG).show()
    }
}
