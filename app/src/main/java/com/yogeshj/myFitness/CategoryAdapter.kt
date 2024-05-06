package com.yogeshj.myFitness

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yogeshj.myFitness.databinding.CategoryRvBinding

class CategoryAdapter(private var dataList: ArrayList<Fitness>, var context: Context) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: CategoryRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        val binding = CategoryRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, position: Int) {
        val link = dataList[position].category.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        Glide.with(context).load(link[1]).into(holder.binding.img)

        holder.binding.title.text = dataList[position].name

        holder.binding.next.setOnClickListener {
            val intent = Intent(context, FitnessActivity::class.java)
            intent.putExtra("link", dataList[position].videoId)
            intent.putExtra("title", dataList[position].name)
            intent.putExtra("steps", dataList[position].steps)
            intent.putExtra("about", dataList[position].ing)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}