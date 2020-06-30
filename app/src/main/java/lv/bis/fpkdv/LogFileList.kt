package lv.bis.fpkdv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import lv.bis.fpkdv.R
import lv.bis.fpkdv.utilit.Translate

class LogFileList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_file_list_layout)

        val itemList = findViewById<ListView>(R.id.ItemListView)
        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.shape))
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = Translate(applicationContext)
            .getTranslatedString(R.array.LogFileActTitle)

        val path = getExternalFilesDir("output")
        output("Path dir: $path")
        if (path != null){
            val fileList = path.list()
            val dataAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1,fileList)
            itemList.adapter = dataAdapter
            itemList.setOnItemClickListener{parent, view, position, id ->
                val intent = Intent(applicationContext,
                    LogFileViewer::class.java)
                intent.putExtra("FileName",fileList[position])
                startActivity(intent)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun output(data:String){
        Log.w("myLittleLog",data)
    }
}