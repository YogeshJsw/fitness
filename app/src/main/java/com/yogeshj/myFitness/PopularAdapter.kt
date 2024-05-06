package com.yogeshj.myFitness

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yogeshj.myFitness.databinding.PopularRvItemsBinding

class PopularAdapter(private var dataList: ArrayList<Fitness>, var context: Context) :
    RecyclerView.Adapter<PopularAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: PopularRvItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PopularRvItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.binding.popularTxt.text = dataList[position].name

        val link = dataList[position].category.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
//        holder.binding.popularDist.text= link[0]
//        Log.d("Popular link", link[1])
        Glide.with(context).load(link[1]).into(holder.binding.popularImg)



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

}