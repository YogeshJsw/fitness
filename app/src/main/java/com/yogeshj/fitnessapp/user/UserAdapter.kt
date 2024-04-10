package com.yogeshj.fitnessapp.user

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.Checkout
import com.yogeshj.fitnessapp.HomeActivity
import com.yogeshj.fitnessapp.PaymentData
import com.yogeshj.fitnessapp.R
import com.yogeshj.fitnessapp.trainer.MeetingLink
import com.yogeshj.fitnessapp.trainer.Trainer
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserAdapter(private val context: Context, private val arr: ArrayList<Trainer>) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name_text)
        val email: TextView = itemView.findViewById(R.id.email)
        val fee: TextView = itemView.findViewById(R.id.fees)
        val pay: ImageView = itemView.findViewById(R.id.select_trainer)
        val clock: ImageView = itemView.findViewById(R.id.clock_img)
        val meetingTitle: TextView = itemView.findViewById(R.id.meeting_title)
        val meetingTxt: TextView = itemView.findViewById(R.id.txtClock)
        val paymentStatus:TextView=itemView.findViewById(R.id.paymentStatus)
        val currCard: ConstraintLayout = itemView.findViewById(R.id.mainCard)
        val meetingLinkTitle:TextView=itemView.findViewById(R.id.meeting_link_title)
        val meetingLink:TextView=itemView.findViewById(R.id.meeting_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_recycler_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = arr[position].name
        holder.email.text = arr[position].email
        holder.fee.text = arr[position].fees.toString()

        if (arr[position].meetingTime?.isNotEmpty() == true) {
            holder.clock.visibility = View.VISIBLE
            holder.meetingTxt.visibility = View.VISIBLE
            holder.meetingTxt.text = "${arr[position].meetingDate} at ${arr[position].meetingTime}"
            holder.meetingTitle.visibility = View.VISIBLE
        } else {
            holder.clock.visibility = View.GONE
            holder.meetingTxt.visibility = View.GONE
            holder.meetingTitle.visibility = View.GONE
        }

        val paymentDb = FirebaseDatabase.getInstance().getReference("Payments")
        paymentDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(paymentSnapshot: DataSnapshot) {
                if (paymentSnapshot.exists()) {
                    for (paymentSnap in paymentSnapshot.children) {
                        val payment = paymentSnap.getValue(PaymentData::class.java)!!
                        if (payment.trainerId == arr[position].uid && payment.userId==HomeActivity.auth.currentUser?.uid) {
//                            holder.currCard.setBackgroundResource(R.drawable.card_shape_green)
                            holder.paymentStatus.text="   Paid     "
                            holder.paymentStatus.setTextColor(context.resources.getColor(R.color.green))
                            holder.pay.setColorFilter(context.resources.getColor(R.color.green))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



        Checkout.preload(context)
        val co = Checkout()
        co.setKeyID("rzp_test_cS4SNLsJzIAjrT")


        holder.pay.setOnClickListener {
            val pay= FirebaseDatabase.getInstance().getReference("Payments")
            pay.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(paymentSnapshot: DataSnapshot) {
                    var paymentFound=false
                    if (paymentSnapshot.exists()) {
                        for (paymentSnap in paymentSnapshot.children) {
                            val payment = paymentSnap.getValue(PaymentData::class.java)!!
                            if (payment.userId == HomeActivity.auth.currentUser?.uid) {
                                Snackbar.make((context as Activity).findViewById(android.R.id.content), "Can only register for 1 meeting at a time.", Snackbar.LENGTH_LONG).show()
                                paymentFound = true
                                break
                            }
                        }
                    }

                    if (!paymentFound) {
                        if (holder.meetingTitle.visibility == View.VISIBLE) {

                            UserMainActivity.id = arr[position].uid
                            UserMainActivity.amount = arr[position].fees.toString().toInt()
                            UserMainActivity.meetingTime=arr[position].meetingTime.toString()
                            UserMainActivity.meetingDate=arr[position].meetingDate.toString()
                            initPayment(position)

                        } else {
                            Snackbar.make(
                                holder.itemView,
                                "Unable to make payment.No meetings scheduled by the current trainer.",
                                Snackbar.LENGTH_LONG
                            ).show()

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun initPayment(position: Int) {
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", "Fitness App")
            options.put("description", "Fees for Trainer: ${arr[position].name}")
            options.put("image", "https://s2.amazon.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", (arr[position].fees.toString().toInt() * 100).toString())

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val preFill = JSONObject()
            preFill.put("email", "${HomeActivity.auth.currentUser?.email}")
//            preFill.put("contact","9876543210")
            preFill.put("trainerId", arr[position].uid)
//            options.put("prefill",preFill)
            co.open(context as Activity?, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}