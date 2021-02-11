package com.abc.reak.todo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomDatePickerAdapter(var context: Context, var list:List<String>, var listener:MyInterface) : RecyclerView.Adapter<CustomDatePickerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateView:TextView = itemView.findViewById(R.id.tv_date);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_view_date_picker, parent, false);
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.dateView.text = list.get(position)
        holder.dateView.setOnClickListener(View.OnClickListener {
            listener.selectDate(list.get(position))
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}