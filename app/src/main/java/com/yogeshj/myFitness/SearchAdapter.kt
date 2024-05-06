package com.yogeshj.myFitness

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yogeshj.myFitness.databinding.SearchRvBinding

class SearchAdapter(private var dataList: ArrayList<Fitness>, var context: Context) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: SearchRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SearchRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val link = dataList[position].category.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        Glide.with(context).load(link[1]).into(holder.binding.img)
        holder.binding.searchTxt.text = dataList[position].name



        holder.itemView.setOnClickListener {
            val intent = Intent(context, FitnessActivity::class.java)
            intent.putExtra("link", dataList[position].videoId)
            intent.putExtra("title", dataList[position].name)
            intent.putExtra("steps", dataList[position].steps)
            intent.putExtra("about", dataList[position].ing)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<Fitness>) {
        dataList = filterList
        notifyDataSetChanged()
    }

}