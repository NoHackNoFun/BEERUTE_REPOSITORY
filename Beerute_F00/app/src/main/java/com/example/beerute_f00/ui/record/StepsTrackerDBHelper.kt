package com.example.beerute_f00.ui.record

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class StepsTrackerDBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    fun createStepsEntry(timeStamp: Long, stepType: Int, sessionId: String?): Boolean {
        var createSuccessful = false
        val mCalendar = Calendar.getInstance()
        val todayDate =
            (mCalendar[Calendar.MONTH] + 1).toString() + "/" + mCalendar[Calendar.DAY_OF_MONTH].toString() + "/" + mCalendar[Calendar.YEAR].toString()
        try {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(STEP_TIME, timeStamp)
            values.put(STEP_DATE, todayDate)
            values.put(STEP_TYPE, stepType)
            values.put(SESSION_ID, sessionId)
            val row = db.insert(TABLE_STEPS_SUMMARY, null, values)
            if (row != -1L) {
                createSuccessful = true
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return createSuccessful
    }

    @SuppressLint("Range") //Hay que mirar esto
    fun getStepsByDate(date: String): IntArray {
        val stepType = IntArray(3)
        val selectQuery =
            "SELECT " + STEP_TYPE + " FROM " + TABLE_STEPS_SUMMARY + " WHERE " + STEP_DATE + " = '" + date + "'"
        try {
            val db = this.readableDatabase
            val c = db.rawQuery(selectQuery, null)
            if (c.moveToFirst()) {
                do {
                    when (c.getInt(c.getColumnIndex(STEP_TYPE))) {
                        WALKING -> ++stepType[0]
                        JOGGING -> ++stepType[1]
                        RUNNING -> ++stepType[2]
                    }
                } while (c.moveToNext())
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stepType
    }

    //For Debug Purposes
    //Hay que mirar esto
    @get:SuppressLint("Range")
    val totalStepsDuration: Long
        get() {
            var totalDuration: Long = 0
            val sessionNameList = ArrayList<String>()
            val stepTimeSessionList = ArrayList<Int>()
            try {
                val selectQuery = "SELECT DISTINCT" + SESSION_ID + " FROM " + TABLE_STEPS_SUMMARY
                val db = this.readableDatabase
                val c = db.rawQuery(selectQuery, null)
                if (c.moveToFirst()) {
                    do {
                        sessionNameList.add(c.getString(c.getColumnIndex(SESSION_ID)))
                    } while (c.moveToNext())
                }
                val sizeSessionNameList = sessionNameList.size
                for (i in 0 until sizeSessionNameList) {
                    val selectTimeQuery =
                        "SELECT " + STEP_TIME + " FROM " + TABLE_STEPS_SUMMARY + " WHERE " + SESSION_ID + " = '" + sessionNameList[i] + "'"
                    val cTime = db.rawQuery(selectTimeQuery, null)
                    if (cTime.moveToFirst()) {
                        do {
                            stepTimeSessionList.add(cTime.getInt(cTime.getColumnIndex(STEP_TIME)))
                        } while (cTime.moveToNext())
                    }
                    val sizeStepTimeSessionList = stepTimeSessionList.size
                    var j = sizeStepTimeSessionList - 1
                    while (j == 1) {
                        totalDuration =
                            totalDuration + stepTimeSessionList[j] - stepTimeSessionList[j - 1]
                        j--
                    }
                }
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return totalDuration
        }

    //For Debug Purposes
    //Hay que mirar esto
    @get:SuppressLint("Range")
    val availableDates: ArrayList<String>
        get() {
            val dateList = ArrayList<String>()
            try {
                val selectQuery = "SELECT DISTINCT" + STEP_DATE + " FROM " + TABLE_STEPS_SUMMARY
                val db = this.readableDatabase
                val c = db.rawQuery(selectQuery, null)
                if (c.moveToFirst()) {
                    do {
                        dateList.add(c.getString(c.getColumnIndex(STEP_DATE)))
                    } while (c.moveToNext())
                }
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dateList
        }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val str = "DROP TABLE IF EXISTS " //Se ha cambiado
        db.execSQL(str + CREATE_TABLE_STEPS_SUMMARY)
        onCreate(db)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_STEPS_SUMMARY)
    }

    companion object {
        private const val DATABASE_NAME = "StepsTrackerDatabase"
        private const val TABLE_STEPS_SUMMARY = "StepsTrackerSummary"
        private const val ID = "id"
        private const val STEP_TYPE = "steptype"
        private const val STEP_TIME = "steptime" //time is in milliseconds Epoch Time
        private const val STEP_DATE = "stepdate" //Date format is mm/dd/yyyy
        private const val SESSION_ID = "sessionid"
        private const val CREATE_TABLE_STEPS_SUMMARY = ("CREATE TABLE "
                + TABLE_STEPS_SUMMARY + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + STEP_DATE + " TEXT," + SESSION_ID + " TEXT," + STEP_TIME + " INTEGER," + STEP_TYPE + " TEXT" + ")")
        private const val DATABASE_VERSION = 1
        private const val RUNNING = 3
        private const val JOGGING = 2
        private const val WALKING = 1
    }
}