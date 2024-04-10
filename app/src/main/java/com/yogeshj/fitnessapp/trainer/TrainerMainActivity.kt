package com.yogeshj.fitnessapp.trainer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.databinding.ActivityTrainerMainBinding
import com.yogeshj.fitnessapp.user.User
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.min

class TrainerMainActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    private lateinit var binding:ActivityTrainerMainBinding
    var day=0
    var month=0
    var year=0
    var hour=0
    var minute=0


    companion object{
        var savedDay=0
        var savedMonth=0
        var savedYear=0
        var savedHour=0
        var savedMinute=0
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTrainerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.set.setOnClickListener {
            getDateTime()
            DatePickerDialog(this,this,year,month,day).show()
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

        val db = FirebaseDatabase.getInstance().getReference("Trainers")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val curr = snap.getValue(Trainer::class.java)!!
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


        binding.confirm.setOnClickListener {


            if (binding.dateAndTime.text.isNotEmpty()) {


                //Adding meeting date and time
                val db= FirebaseDatabase.getInstance().getReference("Trainers")
                db.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            for (snap in snapshot.children)
                            {
                                val curr=snap.getValue(Trainer::class.java)!!
                                if(HomeActivity.auth.currentUser!!.uid==curr.uid)
                                {
                                    val dbRef = FirebaseDatabase.getInstance().getReference("Trainers")
                                    dbRef.child(curr.uid!!).setValue(Trainer(curr.uid!!,curr.name!!,curr.email!!,curr.fees!!,"$savedHour:$savedMinute","$savedYear-$savedMonth-$savedDay"))
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })



                val intent = Intent(this@TrainerMainActivity, TrainerMeetingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


    }

    private fun getDateTime(){
        val cal=Calendar.getInstance()
        day=cal.get(Calendar.DAY_OF_MONTH)
        month=cal.get(Calendar.MONTH)
        year=cal.get(Calendar.YEAR)
        hour=cal.get(Calendar.HOUR)
        minute=cal.get(Calendar.MINUTE)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        savedDay=p3
        savedMonth=p2
        savedYear=p1

        TimePickerDialog(this,this,hour,minute,true).show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        savedHour=p1
        savedMinute=p2

        binding.dateAndTime.text = "$savedDay-$savedMonth-$savedYear\n$savedHour:$savedMinute"
    }
}