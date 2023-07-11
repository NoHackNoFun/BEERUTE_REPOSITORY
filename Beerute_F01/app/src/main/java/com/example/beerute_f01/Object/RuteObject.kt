package com.example.beerute_f01.Object

import android.content.Context
import com.google.gson.Gson

object RuteObject {

    private const val PREF_NAME = "MatrixPrefs"
    private const val KEY_MATRIX = "matrix"

    fun saveMatrix(context: Context, matrix: Array<Array<Double>>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val serializedMatrix = serializeMatrix(matrix)
        editor.putString(KEY_MATRIX, serializedMatrix)
        editor.apply()
    }

    fun getMatrix(context: Context): Array<Array<Double>>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val serializedMatrix = prefs.getString(KEY_MATRIX, null)
        return if (serializedMatrix != null) deserializeMatrix(serializedMatrix) else null
    }

    private fun serializeMatrix(matrix: Array<Array<Double>>): String {
        val gson = Gson()
        return gson.toJson(matrix)
    }

    private fun deserializeMatrix(serializedMatrix: String): Array<Array<Double>> {
        val gson = Gson()
        return gson.fromJson(serializedMatrix, Array<Array<Double>>::class.java)
    }

    fun clearMatrix(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(KEY_MATRIX)
        editor.apply()
    }
}

/*object RuteObject {

    private const val PREF_NAME = "MatrixPrefs"
    private const val KEY_MATRIX = "matrix"

    fun saveMatrix(context: Context, matrix: Array<Array<Double>>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val serializedMatrix = serializeMatrix(matrix)
        editor.putString(KEY_MATRIX, serializedMatrix)
        editor.apply()
    }

    fun getMatrix(context: Context): Array<Array<Double>>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val serializedMatrix = prefs.getString(KEY_MATRIX, null)
        return if (serializedMatrix != null) deserializeMatrix(serializedMatrix) else null
    }

    private fun serializeMatrix(matrix: Array<Array<Double>>): String {
        val gson = Gson()
        return gson.toJson(matrix)
    }

    private fun deserializeMatrix(serializedMatrix: String): Array<Array<Double>> {
        val gson = Gson()
        return gson.fromJson(serializedMatrix, Array<Array<Double>>::class.java)
    }
}*/
