package com.example.fpkdv_kotlinstyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fpkdv_kotlinstyle.utilit.FileLoger
import kotlinx.android.synthetic.main.log_file_viewer_layout.*

class LogFileViewer: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_file_viewer_layout)

        if (intent != null && intent.hasExtra("FileName")){
            val fileName = intent.getStringExtra("FileName")
            if (fileName != null)
                OutputTextBox.text = FileLoger(applicationContext).LoadFile(fileName)
        }

    }
}