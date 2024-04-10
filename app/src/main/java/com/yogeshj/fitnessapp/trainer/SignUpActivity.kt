package com.yogeshj.fitnessapp.trainer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.databinding.ActivitySignUp2Binding
import com.yogeshj.fitnessapp.user.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUp2Binding

    private lateinit var dbRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginTV.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, com.yogeshj.fitnessapp.trainer.LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.nameRegister.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()
            val fees=binding.fees.text.toString().toInt()

            HomeActivity.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task->
                    if (task.isSuccessful) {

                        addUserToDatabase(name, email, HomeActivity.auth.currentUser!!.uid,fees)

                        val intent = Intent(this, TrainerMeetingActivity::class.java)
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
    private fun addUserToDatabase(name: String, email: String, uid: String,fees:Int) {

        dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
        dbRef.child(uid).setValue(Trainer(uid, name, email,fees))
    }
}