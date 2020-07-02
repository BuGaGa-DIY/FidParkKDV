package lv.bis.fpkdv

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
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import lv.bis.fpkdv.R
import lv.bis.fpkdv.utilit.FileLoger
import kotlinx.android.synthetic.main.log_file_viewer_layout.*
import java.net.URLConnection
import java.nio.channels.spi.SelectorProvider.provider

class LogFileViewer: AppCompatActivity() {

    var fileName : String = ""
    lateinit var fileHandler : Handler
    lateinit var parentLayout : LinearLayout
    var myTask : MyTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_file_viewer_layout)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.shape))
        actionBar?.setDisplayHomeAsUpEnabled(true)
        parentLayout = findViewById(R.id.MainTextLayout)

        FileViewerSwipe.isRefreshing = true
        FileViewerSwipe.setOnRefreshListener { FileViewerSwipe.isRefreshing = false }
        fileHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            private fun packData(data :String){
                val tmpTextBox = TextView(applicationContext)
                tmpTextBox.text = data
                tmpTextBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                parentLayout.addView(tmpTextBox)
            }
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    0->{
                        packData(msg.obj.toString())
                        FileViewerSwipe.isRefreshing = false
                        FileViewerSwipe.isEnabled = false
                        ViewerScrollView.post { ViewerScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
                    }
                    1->{
                        packData(msg.obj.toString())
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
            R.id.FileViewerShareMenuItem ->{
                val mainPath = getExternalFilesDir("output/$fileName")
                if (mainPath != null && mainPath.exists()){
                    val logUri = FileProvider.getUriForFile(this,"lv.bis.fpkdv.provider",mainPath)
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/*"//URLConnection.guessContentTypeFromName(mainPath.name)
                        putExtra(Intent.EXTRA_STREAM,logUri)
                    }
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
        override fun doInBackground(vararg params: Void?): Void? {
            FileLoger(applicationContext)
                .LoadFile(fileName,handl)
            return null
        }
    }

    private fun output(data:String){
        Log.w("myLog",data)
    }

}