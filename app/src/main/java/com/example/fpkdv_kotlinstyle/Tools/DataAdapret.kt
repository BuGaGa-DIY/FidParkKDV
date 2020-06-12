package com.example.fpkdv_kotlinstyle.Tools

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.fpkdv_kotlinstyle.R
import com.example.fpkdv_kotlinstyle.Records.DataRecord
import com.example.fpkdv_kotlinstyle.Tools.Enums.AdapterEnums
import com.example.fpkdv_kotlinstyle.utilit.Translate

class DataAdapret(context:Activity, dataList:List<DataRecord>): BaseAdapter() {
    private val mInflator: LayoutInflater

    private val mainContext:Activity = context
    private var mainDataList = dataList
    private var lastSortingState = AdapterEnums.TimeHighToLow.ordinal
    init {
        this.mInflator = LayoutInflater.from(mainContext)
    }

    private class ViewHolder(row: View?){
        var lpnTextBox: TextView? = null
        var timeTextBox: TextView? = null

        init {
            lpnTextBox = row?.findViewById(R.id.item_list_lnp)
            timeTextBox = row?.findViewById(R.id.item_list_time)
        }
    }
    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        var viewHolder: ViewHolder
        if (convertView == null){
            view = mInflator.inflate(R.layout.listitemlayoutitem,null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        }else{
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        view?.setOnClickListener{
            var builder = AlertDialog.Builder(mainContext)
            builder.setTitle("${mainDataList[position].lpn}")
            val fromStr = Translate(mainContext).getTranslatedString(R.array.DialogTimeFrom)
            val toStr = Translate(mainContext).getTranslatedString(R.array.DialogTimeTo)
            builder.setMessage("$fromStr: ${mainDataList[position].timeFrom}\n$toStr: ${mainDataList[position].timeTo}")
            builder.setPositiveButton(Translate(mainContext).getTranslatedString(R.array.DialogOkeyBT)){ dialogInterface, which ->
                dialogInterface.dismiss()
            }
            if(mainDataList[position].timeLeftInMillis<0 && !mainDataList[position].parkingType.equals("Mobilly")){
                builder.setNeutralButton(Translate(mainContext).getTranslatedString(R.array.DialogFineBT)){dialog, which ->
                      dialog.dismiss()
                }
            }
            builder.create().show()
        }

        var simpleDataRecord = mainDataList[position]



        viewHolder.lpnTextBox?.text = simpleDataRecord.lpn

        val timeText = simpleDataRecord.timeLeft
        var timeSpannString = SpannableString(timeText)
        var length = 0
        if (timeText != null) length = timeText.length
        when(timeText?.substring(0,1)){
            "-" -> {
                val cRed = ForegroundColorSpan(ContextCompat.getColor(mainContext,R.color.TimeEndsTextColor))
                timeSpannString.setSpan(cRed,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                viewHolder.timeTextBox?.text = timeSpannString
            }
            "A" -> {
                val cBlue = ForegroundColorSpan(ContextCompat.getColor(mainContext,R.color.AbonnementTextColor))
                timeSpannString.setSpan(cBlue,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                viewHolder.timeTextBox?.text = timeSpannString
            }
            "M" -> {
                val cOrange = ForegroundColorSpan(ContextCompat.getColor(mainContext,R.color.MobillyTextColor))
                timeSpannString.setSpan(cOrange,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                viewHolder.timeTextBox?.text = timeSpannString
            }
            else -> {
                if (timeText!=null && timeText.contains("00:0")){
                    val cYellow = ForegroundColorSpan(ContextCompat.getColor(mainContext,R.color.TimeColseToEndTextColor))
                    timeSpannString.setSpan(cYellow,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    viewHolder.timeTextBox?.text = timeSpannString
                }else{
                    val cGreen = ForegroundColorSpan(ContextCompat.getColor(mainContext,R.color.TimeOKTextColor))
                    timeSpannString.setSpan(cGreen,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    viewHolder.timeTextBox?.text = timeSpannString
                }
            }
        }

        return view as View
    }

    fun sortByTime(dirction:Int){
        if (dirction == AdapterEnums.TimeHighToLow.ordinal) mainDataList = mainDataList.sortedBy { DataRecord -> DataRecord.timeLeftInMillis }
        else mainDataList = mainDataList.sortedByDescending { DataRecord -> DataRecord.timeLeftInMillis }
    }

    fun sortByName(dirction: Int){
        if (dirction == AdapterEnums.AToZ.ordinal) mainDataList = mainDataList.sortedBy { DataRecord -> DataRecord.lpn }
        else mainDataList = mainDataList.sortedByDescending { DataRecord -> DataRecord.lpn }
    }

    private fun lineChanger(id:Int){
        val tmpData = mainDataList.get(id)

    }
    override fun getItem(position: Int): Any {
        return mainDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mainDataList.size
    }
}