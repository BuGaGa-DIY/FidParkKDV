package com.example.fpkdv_kotlinstyle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class LogFileList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_file_list_layout)

        val itemList = findViewById<ListView>(R.id.ItemListView)


        val path = getExternalFilesDir("output")
        output("Path dir: $path")
        if (path != null){
            val fileList = path.list()
            val dataAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1,fileList)
            itemList.adapter = dataAdapter
            itemList.setOnItemClickListener{parent, view, position, id ->
                val intent = Intent(applicationContext,LogFileViewer::class.java)
                intent.putExtra("FileName",fileList[position])
                startActivity(intent)
            }
        }

    }

    private fun output(data:String){
        Log.w("myLittleLog",data)
    }
}