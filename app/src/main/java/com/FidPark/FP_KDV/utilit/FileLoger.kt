package com.FidPark.FP_KDV.utilit

import android.content.Context
import android.util.Log
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FileLoger(val mainContext: Context) {

    fun WriteLine(data :String){
        val path = mainContext.getExternalFilesDir("output")
        //output("Root dir: ${path}")
        if (path != null && !path.exists()) path.mkdir()
        try {
            var format = DateTimeFormatter.ofPattern("dd-MM-YY")
            val fileName = LocalDateTime.now().format(format)+"_LOG.txt"
            val file = File(path,fileName)
            format = DateTimeFormatter.ofPattern("HH:mm:SS.ms")
            val writer = FileWriter(file,true)
            writer.append(LocalDateTime.now().format(format) + " ")
            writer.append(data+"\n")
            writer.flush()
            writer.close()
            output("Line logged to file")
        }catch (e: FileNotFoundException){
            output("File not found $e")
        }
    }

    fun LoadFile(fileName:String):String{
        var result = ""

        val mainPath = mainContext.getExternalFilesDir("output")
        if (mainPath != null && mainPath.exists()){
            try {
                val file = File(mainPath, fileName)
                val reader = FileReader(file)
                while (reader.ready()) result += reader.read().toChar()
                output("File road normal")
            }catch (e : FileNotFoundException){
                output("File not found while trying read file")
            }catch (e : IOException){
                output("Read from file fail: $e")
            }
        }

        return result
    }


    fun output(data:String){
        Log.w("myLog",data)
    }
}