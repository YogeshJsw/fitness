package com.yogeshj.myFitness.user

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yogeshj.myFitness.HomeActivity
import com.yogeshj.myFitness.R
import com.yogeshj.myFitness.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HomeActivity.auth= FirebaseAuth.getInstance()

        initLoadingDialog()


        MobileAds.initialize(this) {}
        val adRequest= AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        binding.loginTV.setOnClickListener {
            showLoading()
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
            hideLoading()
        }

        binding.btnSignUp.setOnClickListener {
            showLoading()
            val name = binding.nameRegister.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                hideLoading()
                Snackbar.make(
                    binding.root,
                    "Name, Email or password cannot be empty.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }


            HomeActivity.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task->
                    if (task.isSuccessful) {

                        addUserToDatabase(name, email, HomeActivity.auth.currentUser!!.uid)

                        val intent = Intent(this, LoginActivity::class.java)
                        if(baseContext!=this@SignUpActivity)
                        {
                            finish()
                        }
                        startActivity(intent)
                        hideLoading()
                    }
                    else
                        hideLoading()
                }.addOnFailureListener {err->
                    hideLoading()
                    Snackbar.make(binding.root, "Error: ${err.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }
    private fun addUserToDatabase(name: String, email: String, uid: String) {

        //To store trainer -> "Trainers" Database(till admin interface is not made)
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(uid).setValue(User(uid, name, email))
    }

    private fun initLoadingDialog() {
        dialog = Dialog(this@SignUpActivity)
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