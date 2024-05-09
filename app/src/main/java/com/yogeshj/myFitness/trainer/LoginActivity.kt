package com.yogeshj.myFitness.trainer

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.myFitness.CheckNetworkConnection
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = HomeActivity.auth.currentUser
        initLoadingDialog()
        showLoading()

        MobileAds.initialize(this) {}
        val adRequest= AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        if (currentUser != null) {
            val db = FirebaseDatabase.getInstance().getReference("Trainers")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(Trainer::class.java)
                            if (curr != null && currentUser.uid == curr.uid) {
                                startActivity(Intent(this@LoginActivity, TrainerMeetingActivity::class.java))
                                finish()
                                break
                            }
                        }
                    }



                    hideLoading()
                }

                override fun onCancelled(error: DatabaseError) {

                    hideLoading()
                }
            })
        } else {
            hideLoading()

        }

        //Internet Status
        var isNetworkConnected = false
        val checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this) { isConnected ->
            isNetworkConnected = isConnected
        }

        binding.btnLogin.setOnClickListener {
            showLoading()

            if (!isNetworkConnected) {
                hideLoading()
                Toast.makeText(this@LoginActivity, "Please check your Internet Connection.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                hideLoading()
                Snackbar.make(
                    binding.emailLogin,
                    "Email or password cannot be empty.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val db = FirebaseDatabase.getInstance().getReference("Trainers")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var flag = false // Move flag declaration here
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(Trainer::class.java)!!
                            if (curr.email.toString() == email) {
                                flag = true
                                break
                            }
                        }
                    }

                    if (!flag) {
                        hideLoading()
                        Snackbar.make(
                            binding.emailLogin,
                            "Incorrect email. Please enter a different email or register as a new trainer.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                            HomeActivity.auth.signOut()
                            HomeActivity.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    hideLoading()
                                    if (task.isSuccessful) {
                                        startActivity(
                                            Intent(
                                                this@LoginActivity,
                                                TrainerMeetingActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }.addOnFailureListener { exception ->
                                    hideLoading()
                                    Toast.makeText(
                                        this@LoginActivity,
                                        exception.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                }
            })
            hideLoading()
        }

    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@LoginActivity)
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