package com.yogeshj.fitnessapp.trainer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.databinding.ActivityLoginBinding
import com.yogeshj.fitnessapp.user.UserMainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val currentUser = HomeActivity.auth.currentUser
        if (currentUser != null) {
            // User is already logged in, proceed to HomeActivity
            val db= FirebaseDatabase.getInstance().getReference("Trainers")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        for (snap in snapshot.children)
                        {
                            val curr=snap.getValue(Trainer::class.java)!!
                            if(currentUser.uid==curr.uid)
                            {
                                startActivity(Intent(this@LoginActivity, TrainerMeetingActivity::class.java))
                                finish()
                                return
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
             // Ensure nothing else in onCreate executes
        }

        binding.btnSignup.visibility = View.GONE




        binding.btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

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
                        Snackbar.make(
                            binding.emailLogin,
                            "Incorrect email. Please enter a different email or register as a new trainer.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        // Execute login logic here, inside onDataChange
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            HomeActivity.auth.signOut()
                            HomeActivity.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
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
                                    Toast.makeText(
                                        this@LoginActivity,
                                        exception.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }

    }
}