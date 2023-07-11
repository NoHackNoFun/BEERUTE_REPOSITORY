package com.example.beerute_f01.StepCounter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.beerute_f01.R

class ListAdapter(var mStepCountList: ArrayList<DateStepsModel>, var mContext: Context) :
    BaseAdapter() {
    var mDateStepCountText: TextView? = null
    var mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mStepCountList.size
    }

    override fun getItem(position: Int): Any {
        return mStepCountList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_rows, parent, false)
        }
        mDateStepCountText = convertView.findViewById<View>(R.id.sensor_name) as TextView
        mDateStepCountText!!.text =
            mStepCountList[position].mDate + " - Total Steps: " + mStepCountList[position].mStepCount.toString()
        return convertView
    }
}