package com.abc.reak.todo

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class ProjectActivity : AppCompatActivity(), MyInterface{

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar:ProgressBar
    private lateinit var recyclerView:RecyclerView
    private lateinit var fab:FloatingActionButton
    private lateinit var adapter:TodoAdapter
    private lateinit var priorityView:TextView
    private lateinit var homeButton:ImageButton
    private lateinit var proName:TextView
    private lateinit var insight:ImageButton

    private var projectId:Int = 0
    private var projectName:String = ""
    private var description:String = ""
    private var deadline:String = ""
    private var starting:String = ""


    private var priority:Int = 9
    private lateinit var dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)


        projectId = intent.extras?.getInt("id") ?: 0
        projectName = intent.extras?.getString("name") ?: "Project Name"
        description = intent.extras?.getString("desc") ?: "Description"
        starting = intent.extras?.getString("start") ?: "Starting date"
        deadline = intent.extras?.getString("end") ?: "Ending date"

        init()

        loadTodo(projectId)

        proName.text = projectName


    }

    private fun loadTodo(currentProject:Int){

        val db = DatabaseHelper(this)
        adapter = TodoAdapter(this, db.loadProjectTodo(project = currentProject), this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        enableSwipeToDelete()

        val progress = db.progressReportProject(currentProject)
        progressBar.max = progress[1]
        progressBar.progress = progress[0]

    }

    private fun init(){

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.rv_todo)
        progressBar = findViewById(R.id.pb_progress)

        homeButton = findViewById(R.id.ib_home_btn)
        proName = findViewById(R.id.tv_current_project)

        insight = findViewById(R.id.ib_insight)
        insight.setOnClickListener { showOptionDialog() }

        homeButton.setOnClickListener { this.finish() }

        fab.setOnClickListener { showAddDialog() }

        val db = DatabaseHelper(this)
        val progress = db.progressReportProject(projectId)
        progressBar.max = progress[1]
        progressBar.progress = progress[0]

    }

    override fun reloadRecyclerView() {
        loadTodo(projectId)
    }

    override fun selectDate(date: String) {

    }

    override fun editTodo(todo: Todo) {
        showEditDialog(todo)
    }

    private fun enableSwipeToDelete() {
        val swipeToDeleteCallback: SwipeCallback = object : SwipeCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item: Todo = adapter.getData().get(position)
                adapter.markAsDone(position)
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }

    fun setPriority(view: View){
        val view: TextView = view as TextView
        priority = view.text.toString().toInt()
        priorityView.text = priority.toString()
        dialog.dismiss()
    }

    private fun showEditDialog(todoItem: Todo){

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_todo)
        dialog.setCancelable(true)

        val todoInput: EditText = dialog.findViewById(R.id.et_todo)
        priorityView = dialog.findViewById(R.id.tv_priority)
        val modify: Button = dialog.findViewById(R.id.add)
        val delete: Button = dialog.findViewById(R.id.cancel)

        todoInput.setText(todoItem.todo)
        priorityView.text = todoItem.priority.toString()

        priorityView.text = todoItem.priority.toString()
        modify.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(this)

            val status = db.editTodo(Todo(id = todoItem.id, todo = todoInput.text.toString(), time = todoItem.time, isCompleted = 0, priority = priorityView.text.toString().toInt(), project = projectId))
            reloadRecyclerView()
            dialog.dismiss()

        })

        delete.setOnClickListener(View.OnClickListener {
            val db = DatabaseHelper(this)
            db.deleteTodo(todoItem.id)
            reloadRecyclerView()
            dialog.dismiss()
        })

        priorityView.setOnClickListener(View.OnClickListener {
            showPriorityDialog()
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

    private fun getCurrentDate():String{

        val pattern = "dd-MM-yyyy"
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val date = LocalDateTime.now()
            val dtf = DateTimeFormatter.ofPattern(pattern)
            date.format(dtf)

        } else {

            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.format(Date())

        }

    }

    private fun showAddDialog(){

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_todo)
        dialog.setCancelable(true)

        val todoInput: EditText = dialog.findViewById(R.id.et_todo)
        priorityView = dialog.findViewById(R.id.tv_priority)
        val add: Button = dialog.findViewById(R.id.add)
        val cancel: Button = dialog.findViewById(R.id.cancel)

        add.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(this)
            db.addTodo(Todo(todo = todoInput.text.toString(), time = getCurrentDate(), isCompleted = 0, priority = priority, project = projectId))
            loadTodo(projectId)
            dialog.dismiss()

        })

        priorityView.setOnClickListener(View.OnClickListener { showPriorityDialog() })

        cancel.setOnClickListener(View.OnClickListener { dialog.dismiss() })

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

    private fun showPriorityDialog(){

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_priority_picker)
        dialog.setCancelable(true)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

    private fun showOptionDialog(){

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_project_option)
        dialog.setCancelable(true)

        val desc = dialog.findViewById<TextView>(R.id.desc)
        val start = dialog.findViewById<TextView>(R.id.starting)
        val dead = dialog.findViewById<TextView>(R.id.deadline)
        val delete = dialog.findViewById<Button>(R.id.delete)
        val share = dialog.findViewById<Button>(R.id.share)

        desc.text = description
        dead.text = deadline
        start.text = starting

        delete.setOnClickListener(View.OnClickListener {
            finish()
            val db = DatabaseHelper(this)
            db.deleteProject(projectId)
        })

        share.setOnClickListener {
            createAndShareProgress()
        }

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

    private fun createAndShareProgress(){


        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type ="text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$projectName -> Progress Report: ${
            (progressBar.progress * 100 / if(progressBar.max == 0) 1 else progressBar.max).toDouble().roundToInt()
        }%")
        startActivity(shareIntent)

    }

    override fun onNavigateUp(): Boolean {
        return super.onNavigateUp()
    }
}

