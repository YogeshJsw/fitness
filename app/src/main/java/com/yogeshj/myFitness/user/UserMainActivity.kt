package com.yogeshj.myFitness.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.PaymentResultWithDataListener
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.PaymentData
import com.yogeshj.myFitness.databinding.ActivityUserMainBinding
import com.yogeshj.myFitness.trainer.Trainer


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

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HomeActivity.auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this@UserMainActivity) {}
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this@UserMainActivity,"ca-app-pub-2095090407853200/1990626448", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })


        binding.back.setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@UserMainActivity)
            }
            finish()
        }



        binding.joinMeeting.setOnClickListener {
            val intent = Intent(this@UserMainActivity, UserMeetingActivity::class.java)
            startActivity(intent)

        }

        binding.history.setOnClickListener {
            if(binding.details.visibility==View.VISIBLE)
            {
                binding.details.visibility=View.GONE
            }
            else
            {
                val paymentText = StringBuilder()
//                val db = FirebaseDatabase.getInstance().getReference("PaymentHistory")
//                db.addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            for (snap in snapshot.children) {
//                                val curr = snap.getValue(PaymentData::class.java)!!
//                                if (HomeActivity.auth.currentUser!!.uid == curr.userId) {
//                                    paymentText+= "\t- ${curr.meetingDate} , ${curr.meetingTime} \t--\t Rs${curr.amount}"
//                                }
//                            }
//                        }
//                        binding.details.text=paymentText
////                        if(paymentText.isEmpty())
////                        {
////                            Snackbar.make(binding.root,"No payment history found.",Snackbar.LENGTH_LONG).show()
////                            binding.details.visibility=View.GONE
////                        }
////                        else
////                        {
////                            binding.details.visibility=View.VISIBLE
////                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
                paymentText.append("Meeting Date & Time\t\t\bAmount\n")
                paymentText.append("------------------------------------------------------\n")
                val dbb= FirebaseDatabase.getInstance().getReference("PaymentHistory")
                dbb.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isPay=false
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                val curr = snap.getValue(PaymentData::class.java)!!
                                if (HomeActivity.auth.currentUser!!.uid == curr.userId) {
                                    isPay=true
                                    paymentText.append("\t${curr.meetingDate}, ${curr.meetingTime}\t\t\t\t\t\t\t\t\tRs${curr.amount}\n")                                }
                            }
                        }
                        if(!isPay)
                        {
                            binding.details.setTextColor(Color.RED)
                            paymentText.setLength(0)
                            Snackbar.make(binding.root,"No payment history found.",Snackbar.LENGTH_LONG).show()
                            binding.details.visibility=View.GONE
                        }
                        else
                        {
                            binding.details.setTextColor(Color.BLACK)
                            binding.details.text = paymentText.toString()
                            binding.details.visibility=View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
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
        val paymentRefUser = FirebaseDatabase.getInstance().getReference("Payment")
        val paymentId = paymentRefUser.push().key
        val paymentData = PaymentData(userId, id, amount, "success", meetingTime, meetingDate,HomeActivity.auth.currentUser?.email)

        val paymentRefHis = FirebaseDatabase.getInstance().getReference("PaymentHistory")
        paymentRefHis.child(paymentId!!).setValue(paymentData)

        paymentRefUser.child(paymentId).setValue(paymentData)
            .addOnSuccessListener {
                Toast.makeText(this@UserMainActivity, "Payment Success", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(this@UserMainActivity, UserMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@UserMainActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }

    }



    override fun onPaymentError(p0: Int, p1: String?, p2: com.razorpay.PaymentData?) {
        Toast.makeText(this@UserMainActivity, "Error: $p1", Toast.LENGTH_LONG).show()
    }
}
