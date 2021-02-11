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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), MyInterface {

    private lateinit var currentDate:TextView
    private lateinit var beforeBtn:ImageButton
    private lateinit var insightBtn:ImageButton
    private lateinit var nextBtn:ImageButton
    private lateinit var recyclerView:RecyclerView
    private lateinit var fab:FloatingActionButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        loadTodo(getCurrentDate())

    }

    private fun init(){

        currentDate = findViewById(R.id.tv_current_date)
        currentDate.setOnClickListener(View.OnClickListener { showDatePickerDialog() })

        beforeBtn = findViewById(R.id.ib_before)
        nextBtn = findViewById(R.id.ib_next)
        insightBtn = findViewById(R.id.ib_insight)
        recyclerView = findViewById(R.id.rv_todo)
        fab = findViewById(R.id.fab)
        progressBar = findViewById(R.id.pb_progress)

        fab.setOnClickListener(View.OnClickListener { showAddDialog() })
        insightBtn.setOnClickListener(View.OnClickListener { showInsightDialog() })

        val db = DatabaseHelper(this)
        val reports = db.progressReportTodo(getCurrentDate());
        progressBar.max = reports[1]
        progressBar.progress = reports[0]



    }

    public fun loadTodo(currentDate: String = getCurrentDate()){

        init()

        val db = DatabaseHelper(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TodoAdapter(this, db.loadTodo(currentDate), this)

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

        val todoInput:EditText = dialog.findViewById(R.id.et_todo)
        val priority:NumberPicker = dialog.findViewById(R.id.np_priority)
        val update:Button = dialog.findViewById(R.id.button)

        priority.maxValue = 9
        priority.minValue = 1

        update.setOnClickListener(View.OnClickListener {

            val db = DatabaseHelper(this)

            val status = db.addTodo(Todo(todo = todoInput.text.toString(), time = getCurrentDate(), isCompleted = 0, priority = priority.value))
            Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()

            loadTodo(getCurrentDate())

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

    private fun showInsightDialog(){

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_insights)
        dialog.setCancelable(true)

        val reportTodays:ProgressBar = dialog.findViewById(R.id.pb_today)
        val reportOverall:ProgressBar = dialog.findViewById(R.id.pb_overall)
        val today:TextView = dialog.findViewById(R.id.tv_today_report)
        val overall:TextView = dialog.findViewById(R.id.tv_overall_report)

        val db = DatabaseHelper(this)
        val reports = db.progressReportTodo(getCurrentDate());

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

    private fun showDatePickerDialog(){

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.setCancelable(true)

        val recyclerView:RecyclerView = dialog.findViewById(R.id.rv_dates)

        val db = DatabaseHelper(this)
        val dates = db.loadDates();

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomDatePickerAdapter(this, dates, this)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window!!.attributes = lp
        dialog.show()

    }

    override fun reloadRecyclerView() {
        loadTodo()
    }

    override fun selectDate(date:String) {
        //do nothing
        currentDate.text = if(date == getCurrentDate()) "Today" else date;
        loadTodo(date)
    }

}