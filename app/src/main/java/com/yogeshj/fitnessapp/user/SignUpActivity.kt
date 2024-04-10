package com.yogeshj.fitnessapp.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.MainActivity
import com.yogeshj.fitnessapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding

    private lateinit var dbRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginTV.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.nameRegister.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()


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

                    }
                }.addOnFailureListener {err->
                    Snackbar.make(binding.root, "Error: ${err.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }
    private fun addUserToDatabase(name: String, email: String, uid: String) {

        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(uid).setValue(User(uid, name, email))
    }
}