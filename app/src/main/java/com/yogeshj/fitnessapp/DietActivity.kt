package com.yogeshj.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.yogeshj.fitnessapp.databinding.ActivityDietActivityBinding

class DietActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDietActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDietActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.expand.setOnClickListener {
            expand(binding.details,binding.expand,binding.collapse)
        }
        binding.collapse.setOnClickListener {
            collapse(binding.details,binding.expand,binding.collapse)
        }

        binding.expand1.setOnClickListener {
            expand(binding.details1,binding.expand1,binding.collapse1)
        }

        binding.collapse1.setOnClickListener {
            collapse(binding.details1,binding.expand1,binding.collapse1)
        }

        binding.expand2.setOnClickListener {
            expand(binding.details2,binding.expand2,binding.collapse2)
        }

        binding.collapse2.setOnClickListener {
            collapse(binding.details2,binding.expand2,binding.collapse2)
        }

        binding.expand3.setOnClickListener {
            expand(binding.details3,binding.expand3,binding.collapse3)
        }

        binding.collapse3.setOnClickListener {
            collapse(binding.details3,binding.expand3,binding.collapse3)
        }

        binding.expand4.setOnClickListener {
            expand(binding.details4,binding.expand4,binding.collapse4)
        }

        binding.collapse4.setOnClickListener {
            collapse(binding.details4,binding.expand4,binding.collapse4)
        }
    }

    private fun collapse(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.GONE
        expand.visibility=View.VISIBLE
        collapse.visibility=View.GONE
    }

    private fun expand(details: TextView, expand: ImageView, collapse: ImageView) {
        details.visibility= View.VISIBLE
        expand.visibility=View.GONE
        collapse.visibility=View.VISIBLE
    }

}