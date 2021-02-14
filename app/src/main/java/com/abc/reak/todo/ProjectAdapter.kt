package com.abc.reak.todo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ProjectAdapter(var context: Context, var list: ArrayList<Project>, var listener: MyInterface) : RecyclerView.Adapter<ProjectAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.list_view_project, parent, false);
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.project.setText(list.get(position).name)
        holder.project.setOnClickListener {
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra("id", list.get(position).id)
            intent.putExtra("name", list.get(position).name)
            intent.putExtra("start", list.get(position).starting)
            intent.putExtra("end", list.get(position).deadline)
            intent.putExtra("desc", list.get(position).desc)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var project = itemView.findViewById<TextView>(R.id.tv_project)
    }

    /*fun markAsDone(position: Int){

        val db = DatabaseHelper(context)
        db.markAsDone(list.get(position).id, if (list.get(position).isCompleted == 1) 0 else 1)
        Toast.makeText(context, if (list.get(position).isCompleted == 1) "Marked As not Done" else "Marked As Done", Toast.LENGTH_SHORT).show()
        listener.reloadRecyclerView()

    }

    fun restoreItem(todo:Todo, position:Int){
        list.add(position, todo)
        listener.reloadRecyclerView()
    }

    fun getData():List<Todo>{
        return list;
    }*/

}