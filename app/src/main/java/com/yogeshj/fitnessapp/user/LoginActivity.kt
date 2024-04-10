package com.yogeshj.fitnessapp.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.R
import com.yogeshj.fitnessapp.databinding.ActivityLogin2Binding
import com.yogeshj.fitnessapp.trainer.Trainer
import com.yogeshj.fitnessapp.trainer.TrainerMainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogin2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        val currentUser = HomeActivity.auth.currentUser
        if (currentUser != null) {
            // User is already logged in, proceed to HomeActivity
            val db= FirebaseDatabase.getInstance().getReference("Users")
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        for (snap in snapshot.children)
                        {
                            val curr=snap.getValue(User::class.java)!!
                            if(currentUser.uid==curr.uid)
                            {
                                startActivity(Intent(this@LoginActivity, UserMainActivity::class.java))
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

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

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
                                if(it.isSuccessful)
                                {
                                    startActivity(Intent(this@LoginActivity,UserMainActivity ::class.java))
                                    finish()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this@LoginActivity,it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}