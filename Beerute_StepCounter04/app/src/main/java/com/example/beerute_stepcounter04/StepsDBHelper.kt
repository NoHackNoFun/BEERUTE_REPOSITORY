package com.example.beerute_stepcounter04

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class StepsDBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_STEPS_SUMMARY)
    }

    @SuppressLint("Range") //Se debe volver a mirar esto
    fun createStepsEntry(): Boolean {
        var isDateAlreadyPresent = false
        var createSuccessful = false
        var currentDateStepCounts = 0
        val mCalendar = Calendar.getInstance()
        val todayDate =
            mCalendar[Calendar.MONTH].toString() + "/" + (mCalendar[Calendar.DAY_OF_MONTH] + 1).toString() + "/" + mCalendar[Calendar.YEAR].toString()
        val selectQuery = "SELECT " + STEPS_COUNT + " FROM " +
                TABLE_STEPS_SUMMARY + " WHERE " + CREATION_DATE + " = '" + todayDate + "'"
        try {
            val db = this.readableDatabase
            val c = db.rawQuery(selectQuery, null)
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true
                    currentDateStepCounts = c.getInt(c.getColumnIndex(STEPS_COUNT))
                } while (c.moveToNext())
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(CREATION_DATE, todayDate)
            if (isDateAlreadyPresent) {
                values.put(STEPS_COUNT, ++currentDateStepCounts)
                val row = db.update(
                    TABLE_STEPS_SUMMARY,
                    values,
                    CREATION_DATE + " = '" + todayDate + "'",
                    null
                )
                if (row == 1) {
                    createSuccessful = true
                }
                db.close()
            } else {
                values.put(STEPS_COUNT, 1)
                val row = db.insert(TABLE_STEPS_SUMMARY, null, values)
                if (row != -1L) {
                    createSuccessful = true
                }
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return createSuccessful
    }

    @SuppressLint("Range") //Se debe volver a mirar esto
    fun readStepsEntries(): ArrayList<DateStepsModel> {
        val mStepCountList = ArrayList<DateStepsModel>()
        val selectQuery = "SELECT * FROM " + TABLE_STEPS_SUMMARY
        try {
            val db = this.readableDatabase
            val c = db.rawQuery(selectQuery, null)
            if (c.moveToFirst()) {
                do {
                    val mDateStepsModel = DateStepsModel()
                    mDateStepsModel.mDate = c.getString(c.getColumnIndex(CREATION_DATE))
                    mDateStepsModel.mStepCount = c.getInt(c.getColumnIndex(STEPS_COUNT))
                    mStepCountList.add(mDateStepsModel)
                } while (c.moveToNext())
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mStepCountList
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_STEPS_SUMMARY)
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "StepsDatabase"
        private const val TABLE_STEPS_SUMMARY = "StepsSummary"
        private const val ID = "id"
        private const val STEPS_COUNT = "stepscount"
        private const val CREATION_DATE = "creationdate"
        private const val CREATE_TABLE_STEPS_SUMMARY = ("CREATE TABLE "
                + TABLE_STEPS_SUMMARY + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CREATION_DATE + " TEXT," + STEPS_COUNT + " INTEGER" + ")")
    }
}