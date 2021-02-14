package com.abc.reak.todo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), MyInterface {

    private lateinit var currentDate: TextView
    private lateinit var beforeBtn: ImageButton
    private lateinit var insightBtn: ImageButton
    private lateinit var nextBtn: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var fabProject: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var projectRecyclerView: RecyclerView

    private lateinit var dialog: Dialog
    private lateinit var priorityView: TextView

    private var priority: Int = 9

    private var currentSelectedDate: String = "0";

    private lateinit var adapter: TodoAdapter
    private lateinit var adapterProject: ProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        loadTodo(getCurrentDate())
        currentSelectedDate = getCurrentDate()

        loadProjects()

    }

    private fun init() {

        currentDate = findViewById(R.id.tv_current_date)
        currentDate.setOnClickListener(View.OnClickListener { showDatePickerDialog() })

        beforeBtn = findViewById(R.id.ib_before)
        nextBtn = findViewById(R.id.ib_next)
        insightBtn = findViewById(R.id.ib_insight)
        recyclerView = findViewById(R.id.rv_todo)
        fab = findViewById(R.id.fab)
        progressBar = findViewById(R.id.pb_progress)
        coordinatorLayout = findViewById(R.id.coordinator)
        projectRecyclerView = findViewById(R.id.rv_project)

        fabProject = findViewById(R.id.fab_project)
        fabProject.setOnClickListener { showAddProjectDialog() }

        fab.setOnClickListener { showAddDialog() }
        insightBtn.setOnClickListener { showInsightDialog() }

        val db = DatabaseHelper(this)
        val reports = db.progressReportTodo(currentSelectedDate);
        progressBar.max = reports[1]
        progressBar.progress = reports[0]

    }

    private fun loadProjects() {
        init()
        val db = DatabaseHelper(this)
        adapterProject = ProjectAdapter(this, db.loadProject(), this)
        projectRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        projectRecyclerView.adapter = adapterProject
    }

    private fun loadTodo(currentDate: String = getCurrentDate()) {

        init()
        val db = DatabaseHelper(this)
        adapter = TodoAdapter(this, db.loadTodo(currentDate, 0), this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        enableSwipeToDelete()


    }

    override fun reloadRecyclerView() {
        loadTodo()
    }

    override fun selectDate(date: String) {
        //do nothing
        currentDate.text = if (date == getCurrentDate()) "Today" else date
        currentSelectedDate = date
        loadTodo(date)
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

    fun setPriority(view: View) {
        val view: TextView = view as TextView
        priority = view.text.toString().toInt()
        priorityView.text = priority.toString()
        dialog.dismiss()
    }

    private fun showEditDialog(todoItem: Todo) {

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

            val status = db.editTodo(
                Todo(
                    id = todoItem.id,
                    todo = todoInput.text.toString(),
                    time = todoItem.time,
                    isCompleted = 0,
                    priority = priorityView.text.toString().toInt(),
                    project = 0
                )
            )
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

    private fun getCurrentDate(): String {

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

    private fun getCurrentDateWithTime(): String {

        val pattern = "dd-MM-yyyy HH:mm a"
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val date = LocalDateTime.now()
            val dtf = DateTimeFormatter.ofPattern(pattern)
            date.format(dtf)

        } else {

            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.format(Date())

        }

    }

    private fun showAddProjectDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_project)
        dialog.setCancelable(true)

        val titlePro: EditText = dialog.findViewById(R.id.et_title)
        val descPro: EditText = dialog.findViewById(R.id.et_desc)
        val startPro: EditText = dialog.findViewById(R.id.et_starting)
        val deadPro: EditText = dialog.findViewById(R.id.et_deadline)

        priorityView = dialog.findViewById(R.id.tv_priority)
        val add: Button = dialog.findViewById(R.id.add)
        val cancel: Button = dialog.findViewById(R.id.cancel)

        startPro.setText(getCurrentDateWithTime())
        deadPro.setText(getCurrentDateWithTime())

        add.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(this)
            db.addProject(
                Project(
                    id = 0,
                    name = titlePro.text.toString(),
                    isCompleted = 0,
                    priority = priority,
                    starting = startPro.text.toString(),
                    deadline = deadPro.text.toString(),
                    desc = descPro.text.toString()
                )
            )
            loadProjects()
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

    private fun showAddDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_todo)
        dialog.setCancelable(true)

        val todoInput: EditText = dialog.findViewById(R.id.et_todo)
        priorityView = dialog.findViewById(R.id.tv_priority)
        val add: Button = dialog.findViewById(R.id.add)
        val cancel: Button = dialog.findViewById(R.id.cancel)

        add.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(this)
            db.addTodo(
                Todo(
                    todo = todoInput.text.toString(),
                    time = getCurrentDate(),
                    isCompleted = 0,
                    priority = priority,
                    project = 0
                )
            )
            loadTodo(getCurrentDate())
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

    override fun onResume() {
        super.onResume()
        loadProjects()
    }

    private fun showPriorityDialog() {

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

    private fun showInsightDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_insights)
        dialog.setCancelable(true)

        val reportTodays: ProgressBar = dialog.findViewById(R.id.pb_today)
        val reportOverall: ProgressBar = dialog.findViewById(R.id.pb_overall)
        val today: TextView = dialog.findViewById(R.id.tv_today_report)
        val overall: TextView = dialog.findViewById(R.id.tv_overall_report)

        val db = DatabaseHelper(this)
        val reports = db.progressReportTodo(currentSelectedDate);

        reportTodays.max = reports[1]
        reportTodays.progress = reports[0]

        reportOverall.max = reports[3]
        reportOverall.progress = reports[2]

        today.text = "${reports[0]} / ${reports[1]}"
        overall.text = "${reports[2]} / ${reports[3]}"

        Toast.makeText(this, "${reports[0]} : ${reports[1]}", Toast.LENGTH_SHORT).show()

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

    private fun showDatePickerDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.setCancelable(true)

        val recyclerView: RecyclerView = dialog.findViewById(R.id.rv_dates)

        val db = DatabaseHelper(this)
        val dates = db.loadDates();

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomDatePickerAdapter(this, dates, this)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.TOP)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

}