package com.yogeshj.myFitness.user

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.myFitness.CheckNetworkConnection
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivityLogin2Binding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogin2Binding
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        HomeActivity.auth= FirebaseAuth.getInstance()

        MobileAds.initialize(this) {}
        val adRequest= AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        initLoadingDialog()
        showLoading()

        val currentUser = HomeActivity.auth.currentUser
        if (currentUser != null) {
            val db = FirebaseDatabase.getInstance().getReference("Users")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(User::class.java)
                            if (curr != null && currentUser.uid == curr.uid) {
                                startActivity(Intent(this@LoginActivity, UserMainActivity::class.java))
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


        binding.btnSignup.setOnClickListener {
            showLoading()
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            finish()
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


            val db = FirebaseDatabase.getInstance().getReference("Users")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var flag = false
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val curr = snap.getValue(User::class.java)!!
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
                            "Incorrect email. Please enter a different email or register as a new user.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        if(email.isNotEmpty() && password.isNotEmpty())
                        {
                            HomeActivity.auth.signOut()
                            HomeActivity.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                                hideLoading()
                                if(it.isSuccessful)
                                {
                                    startActivity(Intent(this@LoginActivity,UserMainActivity ::class.java))
                                    finish()
                                }
                            }.addOnFailureListener {
                                hideLoading()
                                Toast.makeText(this@LoginActivity,it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoading()
                }
            })
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