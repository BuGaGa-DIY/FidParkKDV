package com.FidPark.FP_KDV.Records

import android.util.Log
import java.lang.Exception
import java.util.*

class DataRecord(val lpn:String, val timeFrom:String, val timeTo:String, val parkingType:String) {

    var timeLeft:String? = ""
    var timeLeftInMillis:Long = 0
    init {
        getTimeLeft()
    }

    fun getTimeLeft(){
        when(parkingType){
            "Mobilly" -> {
                timeLeft = "M"
                timeLeftInMillis = -1
            }
            "subscription - admin" -> {
                timeLeft = "A"
                timeLeftInMillis = 100000
            }

            else -> timeLeft = calcTime()
        }

    }

    private fun calcTime():String? {
        try {
            val currentTime = Calendar.getInstance()
            val tempTime = Calendar.getInstance()
            val year = timeTo.substring(6, 10).toInt()
            val month = timeTo.substring(3, 5).toInt() - 1
            val day = timeTo.substring(0, 2).toInt()
            val HH = timeTo.substring(11, 13).toInt()
            val MM = timeTo.substring(14, 16).toInt()
            val SS = timeTo.substring(17, 19).toInt()
            tempTime.set(year, month, day, HH, MM, SS)
            var millis = tempTime.timeInMillis - currentTime.timeInMillis
            var _timeIsOver = false
            if (millis<0){
                _timeIsOver = true
                millis *= -1
            }
            millis /= 60000
            if(_timeIsOver) timeLeftInMillis = millis* -1
            else timeLeftInMillis = millis
            val min = millis % 60
            millis /= 60
            val hou = millis % 24
            millis /= 24
            var tempTimeto = ""
            if (_timeIsOver) tempTimeto += "-"
            if (millis > 0){
                tempTimeto += "$millis"
                tempTimeto += "d "
                return tempTimeto
            }
            if (hou < 10) tempTimeto += "0$hou:"
            else tempTimeto += "$hou:"
            if (min < 10) tempTimeto += "0$min"
            else tempTimeto += "$min"

            return tempTimeto
        }catch (e:Exception){
            output("Calculating date error: ${e.message}\n Failed while parsing this data: $timeTo")
            return "CalcErr"
        }
    }

    private fun output(data:String){
        Log.w("myLittleLog",data)
    }
}