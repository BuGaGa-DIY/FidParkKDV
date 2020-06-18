package com.example.fpkdv_kotlinstyle

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.fpkdv_kotlinstyle.utilit.FileLoger
import kotlinx.android.synthetic.main.log_file_viewer_layout.*
import java.net.URLConnection

class LogFileViewer: AppCompatActivity() {

    var fileName : String = ""
    lateinit var fileHandler : Handler
    var myTask : MyTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_file_viewer_layout)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.shape))
        actionBar?.setDisplayHomeAsUpEnabled(true)


        FileViewerSwipe.isRefreshing = true
        FileViewerSwipe.setOnRefreshListener { FileViewerSwipe.isRefreshing = false }
        fileHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    0->{
                        OutputTextBox.text = msg.obj.toString()
                        FileViewerSwipe.isRefreshing = false
                    }
                }
                super.handleMessage(msg)
            }
        }
        if (intent != null && intent.hasExtra("FileName")){
            fileName = intent.getStringExtra("FileName")!!
            actionBar?.title = fileName
            myTask = MyTask(fileHandler)
            myTask?.execute()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.file_viwer_share_file_menu,menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.FileViewerShareMenuItem->{
                val mainPath = getExternalFilesDir("output/$fileName")
                if (mainPath != null && mainPath.exists()){
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = URLConnection.guessContentTypeFromName(mainPath.name)
                    shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(mainPath.absolutePath))
                    startActivity(Intent.createChooser(shareIntent, "Send Log with"))
                }
                else output("File path null or don't exist")
            }
            else->{
                myTask?.cancel(false)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("StaticFieldLeak")
    inner class MyTask(var handl: Handler) : AsyncTask<Void,Void,Void>(){

        var str :String = ""
        override fun doInBackground(vararg params: Void?): Void? {
            str = FileLoger(applicationContext).LoadFile(fileName)

            return null
        }

        override fun onPostExecute(result: Void?) {
            handl.sendMessage(Message.obtain(handl,0,str))

            super.onPostExecute(result)
        }

    }

    private fun output(data:String){
        Log.w("myLog",data)
    }

}