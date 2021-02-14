package com.abc.reak.todo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_NAME = "TODO_DB"
        private const val DATABASE_VERSION = 1
        private const val TODO_TABLE = "TODO_TABLE"
        private const val TODO_ID = "TODO_ID"
        private const val TODO = "TODO"
        private const val IS_COMPLETED = "IS_COMPLETED"
        private const val PRIORITY = "PRIORITY"

        private const val PROJECT_TABLE = "PROJECT_TABLE"
        private const val PROJECT_ID = "PROJECT_ID"
        private const val PROJECT_NAME = "NAME"
        private const val PROJECT_DESC = "DESCRIPTION"
        private const val PROJECT_DEADLINE = "DEADLINE"
        private const val PROJECT_STARTING = "STARTING"
        private const val PROJECT_IS_COMPLETED = "IS_COMPLETED"
        private const val PROJECT_PRIORITY = "PRIORITY"

        private const val DATE_TABLE = "DATE_TABLE"
        private const val DATE_ID = "DATE_ID"
        private const val DATE = "DATE"

        private const val TIME = "TIME"
        
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTodoTableQuery = "CREATE TABLE $TODO_TABLE ($TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT, $TODO TEXT, $TIME TEXT, $IS_COMPLETED INTEGER, $PRIORITY INTEGER, $PROJECT_ID INTEGER)"
        db?.execSQL(createTodoTableQuery)

        val createDateTableQuery = "CREATE TABLE $DATE_TABLE ($DATE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $DATE TEXT)"
        db?.execSQL(createDateTableQuery)

        val createProjectTableQuery = "CREATE TABLE $PROJECT_TABLE ($PROJECT_ID INTEGER PRIMARY KEY AUTOINCREMENT, $PROJECT_NAME TEXT, $PROJECT_DESC TEXT, $IS_COMPLETED INTEGER, $PRIORITY INTEGER, $PROJECT_STARTING TEXT, $PROJECT_DEADLINE TEXT)"
        db?.execSQL(createProjectTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //do nothing here..
    }

    fun addTodo(todo:Todo):Long{

        val db = writableDatabase

        val contentValues = ContentValues()
        contentValues.put(TODO, todo.todo)
        contentValues.put(TIME, todo.time)
        contentValues.put(IS_COMPLETED, todo.isCompleted)
        contentValues.put(PRIORITY, todo.priority)
        contentValues.put(PROJECT_ID, todo.project)

        addNewDate(todo.time)
        return db.insert(TODO_TABLE, null, contentValues)

    }

    fun editTodo(todo:Todo): Boolean{

        val db = writableDatabase
        val editQuery = "UPDATE $TODO_TABLE SET $TODO = '${todo.todo.replace("'", "''")}', $PRIORITY = '${todo.priority}' WHERE $TODO_ID = '${todo.id}'"
        val cursor = db.rawQuery(editQuery, null)
        return cursor.moveToFirst()

    }

    fun markAsDone(id:Int, isCompleted:Int):Boolean{
        val db = writableDatabase
        val update = "UPDATE $TODO_TABLE SET $IS_COMPLETED = '$isCompleted' WHERE $TODO_ID = '${id}'"
        //val deleteQuery = "DELETE FROM $TODO_TABLE WHERE $TODO_ID = '$id'"
        val cursor = db.rawQuery(update, null)
        return cursor.moveToFirst()
    }

    fun deleteTodo(id:Int):Boolean{
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TODO_TABLE WHERE $TODO_ID = '$id'"
        val cursor = db.rawQuery(deleteQuery, null)
        return cursor.moveToFirst()
    }

    fun loadTodo(time:String, project:Int): ArrayList<Todo>{
        val list = ArrayList<Todo>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TODO_TABLE WHERE $TIME = '$time' AND $PROJECT_ID = '$project' ORDER BY $IS_COMPLETED ASC, $PRIORITY DESC"
        var cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)))
            }while (cursor.moveToNext())
        }

        return list
    }

    fun loadProjectTodo(project:Int): ArrayList<Todo>{
        val list = ArrayList<Todo>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TODO_TABLE WHERE $PROJECT_ID = '$project' ORDER BY $IS_COMPLETED ASC, $PRIORITY DESC"
        var cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)))
            }while (cursor.moveToNext())
        }

        return list
    }

    fun progressReportTodo(time:String):List<Int> {

        val sumQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $TIME = '$time' AND $PROJECT_ID = '0'"
        val cur = readableDatabase.rawQuery(sumQuery,null)
        val total = if (cur.moveToFirst()) cur.getInt(cur.getColumnIndex("TOTAL")) else 0

        val completedQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $TIME = '$time' AND $IS_COMPLETED = '1' AND $PROJECT_ID = '0'"
        val cursor = readableDatabase.rawQuery(completedQuery,null)
        val completed = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndex("TOTAL")) else 0

        val sumQueryOverall = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $PROJECT_ID = '0'"
        val curOverall = readableDatabase.rawQuery(sumQueryOverall,null)
        val totalOverall = if (curOverall.moveToFirst()) curOverall.getInt(cur.getColumnIndex("TOTAL")) else 0

        val completedQueryOverall = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $IS_COMPLETED = '1' AND $PROJECT_ID = '0'"
        val cursorOverall = readableDatabase.rawQuery(completedQueryOverall,null)
        val completedOverall = if (cursorOverall.moveToFirst()) cursorOverall.getInt(cursor.getColumnIndex("TOTAL")) else 0

        return listOf(completed, total, completedOverall, totalOverall)
    }

    private fun addNewDate(date:String){

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $DATE_TABLE WHERE $DATE = '$date'"
        var cursor = db.rawQuery(selectQuery, null)

        if (!cursor.moveToFirst()) {

            val db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put(DATE, date)
            db.insert(DATE_TABLE, null, contentValues)

        }

    }

    fun loadDates(): List<String>{

        val list = mutableListOf<String>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $DATE_TABLE"
        var cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(cursor.getString(1))
            }while (cursor.moveToNext())
        }

        return list
    }

    fun addProject(project: Project):Long{

        val db = writableDatabase

        val contentValues = ContentValues()
        contentValues.put(PROJECT_NAME, project.name)
        contentValues.put(PROJECT_DESC, project.desc)
        contentValues.put(PROJECT_IS_COMPLETED, project.isCompleted)
        contentValues.put(PROJECT_PRIORITY, project.priority)
        contentValues.put(PROJECT_STARTING, project.starting)
        contentValues.put(PROJECT_DEADLINE, project.deadline)

        return db.insert(PROJECT_TABLE, null, contentValues)

    }

    fun editProject(project: Project): Boolean{

        val db = writableDatabase
        val editQuery = "UPDATE $PROJECT_TABLE SET $PROJECT_NAME = '${project.name}', $PROJECT_DESC = '${project.desc}' WHERE $PROJECT_ID = '${project.id}'"
        val cursor = db.rawQuery(editQuery, null)
        return cursor.moveToFirst()

    }

    fun deleteProject(id:Int):Boolean{
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $PROJECT_TABLE WHERE $PROJECT_ID = '$id'"
        val cursor = db.rawQuery(deleteQuery, null)
        return cursor.moveToFirst()
    }

    fun loadProject(): ArrayList<Project>{
        val list = ArrayList<Project>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $PROJECT_TABLE ORDER BY $IS_COMPLETED ASC, $PRIORITY DESC"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(Project(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5),cursor.getString(6)))
            }while (cursor.moveToNext())
        }

        return list
    }

    fun progressReportProject(id:Int):List<Int> {

        val sumQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $PROJECT_ID = '$id'"
        val cur = readableDatabase.rawQuery(sumQuery,null)
        val total = if (cur.moveToFirst()) cur.getInt(cur.getColumnIndex("TOTAL")) else 0

        val completedQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $PROJECT_ID = '$id' AND $IS_COMPLETED = '1'"
        val cursor = readableDatabase.rawQuery(completedQuery,null)
        val completed = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndex("TOTAL")) else 0

        return listOf(completed, total)
    }

}