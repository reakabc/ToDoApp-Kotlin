package com.abc.reak.todo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(var context: Context, var list:List<Todo>, var reloadListener:MyInterface) : RecyclerView.Adapter<TodoAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.list_view_todo, parent, false);
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.todo.setText(list.get(position).todo)
        if (list.get(position).isCompleted == 1){
            holder.view.setBackgroundColor(Color.GREEN)
        }else{
            holder.view.setBackgroundColor(Color.RED)
        }
        holder.cv.setOnClickListener(View.OnClickListener { showEditDialog(list.get(position)) })
        holder.cv.setOnLongClickListener(View.OnLongClickListener {
            val db = DatabaseHelper(context)
            db.markAsDone(list.get(position).id, if (list.get(position).isCompleted == 1) 0 else 1)
            Toast.makeText(context, if (list.get(position).isCompleted == 1) "Marked As not Done" else "Marked As Done", Toast.LENGTH_SHORT).show()
            reloadListener.reloadRecyclerView()
            return@OnLongClickListener true
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var todo = itemView.findViewById<TextView>(R.id.tv_todo)
        var view = itemView.findViewById<View>(R.id.status_view)
        var cv = itemView.findViewById<CardView>(R.id.cv_item)
    }

    private fun showEditDialog(todoItem: Todo){

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_edit_todo)
        dialog.setCancelable(true)

        val todo: EditText = dialog.findViewById(R.id.et_todo)
        val priority: NumberPicker = dialog.findViewById(R.id.np_priority)
        val update: Button = dialog.findViewById(R.id.button)

        priority.maxValue = 9
        priority.minValue = 1

        todo.setText(todoItem.todo)
        priority.value = todoItem.priority

        update.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(context)

            val status = db.editTodo(Todo(id = todoItem.id, todo = todo.text.toString(), time = todoItem.time, isCompleted = 0, priority = priority.value))
            Toast.makeText(context, status.toString(), Toast.LENGTH_SHORT).show()

            reloadListener.reloadRecyclerView()
            dialog.dismiss()

        })

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

}