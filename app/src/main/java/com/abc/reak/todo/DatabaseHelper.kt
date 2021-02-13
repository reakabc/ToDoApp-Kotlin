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
        
        /*private const val NOTE_TABLE = "NOTE_TABLE"
        private const val NOTE_ID = "NOTE_ID"
        private const val NOTE = "NOTE"*/

        private const val DATE_TABLE = "DATE_TABLE"
        private const val DATE_ID = "DATE_ID"
        private const val DATE = "DATE"

        private const val TIME = "TIME"
        
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTodoTableQuery = "CREATE TABLE $TODO_TABLE ($TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT, $TODO TEXT, $TIME TEXT, $IS_COMPLETED INTEGER, $PRIORITY INTEGER)"
        db?.execSQL(createTodoTableQuery)

        val createDateTableQuery = "CREATE TABLE $DATE_TABLE ($DATE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $DATE TEXT)"
        db?.execSQL(createDateTableQuery)

        /*val createNoteTableQuery = "CREATE TABLE $NOTE_TABLE ($NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT, $NOTE TEXT, $TIME TEXT)"
        db?.execSQL(createNoteTableQuery)*/
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

        addNewDate(todo.time)
        return db.insert(TODO_TABLE, null, contentValues)

    }

    fun editTodo(todo:Todo): Boolean{

        val db = writableDatabase
        val editQuery = "UPDATE $TODO_TABLE SET $TODO = '${todo.todo}', $PRIORITY = '${todo.priority}' WHERE $TODO_ID = '${todo.id}'"
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

    fun loadTodo(time:String): ArrayList<Todo>{
        val list = ArrayList<Todo>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TODO_TABLE WHERE $TIME = '$time' ORDER BY $IS_COMPLETED ASC, $PRIORITY DESC"
        var cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4)))
            }while (cursor.moveToNext())
        }

        return list
    }

    fun progressReportTodo(time:String):List<Int> {

        val sumQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $TIME = '$time'"
        val cur = readableDatabase.rawQuery(sumQuery,null)
        val total = if (cur.moveToFirst()) cur.getInt(cur.getColumnIndex("TOTAL")) else 0

        val completedQuery = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $TIME = '$time' AND $IS_COMPLETED = '1'"
        val cursor = readableDatabase.rawQuery(completedQuery,null)
        val completed = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndex("TOTAL")) else 0

        val sumQueryOverall = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE"
        val curOverall = readableDatabase.rawQuery(sumQueryOverall,null)
        val totalOverall = if (curOverall.moveToFirst()) curOverall.getInt(cur.getColumnIndex("TOTAL")) else 0

        val completedQueryOverall = "SELECT SUM($PRIORITY) as TOTAL FROM $TODO_TABLE WHERE $IS_COMPLETED = '1'"
        val cursorOverall = readableDatabase.rawQuery(completedQueryOverall,null)
        val completedOverall = if (cursorOverall.moveToFirst()) cursorOverall.getInt(cursor.getColumnIndex("TOTAL")) else 0

        return listOf(completed, total, completedOverall, totalOverall)
    }

    fun addNewDate(date:String){

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

    /*fun addNote(note:Note): Long{

        val db = writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NOTE, note.note)
        contentValues.put(TIME, note.time)

        return db.insert(NOTE_TABLE, null, contentValues)

    }

    fun editNote(note:Note): Boolean{

        val db = writableDatabase
        val editQuery = "UPDATE $NOTE_TABLE SET $NOTE = ${note.note} WHERE $NOTE_ID = ${note.id}"
        var cursor = db.rawQuery(editQuery, null)
        return cursor.moveToFirst()

    }

    fun deleteNote(id:Int):Boolean{
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $NOTE_TABLE WHERE $NOTE_ID = $id"
        val cursor = db.rawQuery(deleteQuery, null)
        return cursor.moveToFirst()
    }

    fun loadNote(): List<Note>{
        val list = mutableListOf<Note>();

        val db = readableDatabase
        val selectQuery = "SELECT * FROM $NOTE_TABLE"
        var cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                list.add(Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
            }while (cursor.moveToNext())
        }

        return list
    }*/

}