package com.example.fpkdv_kotlinstyle

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.fpkdv_kotlinstyle.Records.DataRecord
import com.example.fpkdv_kotlinstyle.Tools.Client
import com.example.fpkdv_kotlinstyle.Tools.Enums.whatStait

class CheckOneLpnActivity : AppCompatActivity() {
    private val context = this
    lateinit private var lpnInput:EditText;
    lateinit private var sendBT:Button;

    private var handler:Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chack_one_lpn)
        lpnInput = findViewById(R.id.inputLPN)
        sendBT = findViewById(R.id.checkOneLPNBT)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.shape))
        actionBar?.setDisplayHomeAsUpEnabled(true)

        handler = @SuppressLint("HandlerLeak")
        object :Handler(){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what){
                    whatStait.RequestFail.ordinal -> Toast.makeText(context,"Connection fail", Toast.LENGTH_SHORT).show()
                    whatStait.GetOneReadyTrue.ordinal -> {
                        showMsg(true,msg.obj as DataRecord)
                    }
                    whatStait.GetOneReadyFalse.ordinal -> {
                        showMsg(false,null)
                    }
                }
            }
        }

        sendBT.setOnClickListener {
            if (lpnInput.text.toString() != "") {
                val client = Client(this, handler, null, null)
                client.sendRequestGetOne(lpnInput.text.toString())
            }else{

            }
        }

    }

    private fun showMsg(response: Boolean,data: DataRecord?){
        val builder = AlertDialog.Builder(context)
        if (response){
            builder.setTitle(data?.lpn)
            builder.setMessage("From: ${data?.timeFrom}\nTo: ${data?.timeTo}")
        }else{
            builder.setTitle("Car not in DB")
        }
        builder.setPositiveButton("Ok"){dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}
