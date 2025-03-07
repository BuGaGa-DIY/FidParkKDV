package lv.bis.fpkdv

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import lv.bis.fpkdv.R
import lv.bis.fpkdv.Records.DataRecord
import lv.bis.fpkdv.Tools.Client
import lv.bis.fpkdv.Tools.Enums.whatStait
import lv.bis.fpkdv.utilit.Translate

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
                    whatStait.GetOneReadyMobillyError.ordinal -> {
                        responsMobillyErrorWindow()
                    }
                    whatStait.UnexpectedPackage.ordinal -> {
                        responseErrorAlertWindow()
                    }
                }
            }
        }

        sendBT.setOnClickListener {
            if (lpnInput.text.toString() != "") {
                val client =
                    Client(this, handler, null, null)
                client.sendRequestGetOne(lpnInput.text.toString())
            }else{

            }
        }
        sendBT.text = getTranslatedString(R.array.SendBT)

    }

    private fun showMsg(response: Boolean,data: DataRecord?){
        val builder = AlertDialog.Builder(context)
        if (response){
            builder.setTitle(data?.lpn)
            val fromStr = Translate(context)
                .getTranslatedString(R.array.DialogTimeFrom)
            val toStr = Translate(context)
                .getTranslatedString(R.array.DialogTimeTo)
            if (data?.parkingType == "Mobilly"){
                val mobClientTmpStr = getTranslatedString(R.array.MobillyClientString)
                builder.setMessage(mobClientTmpStr + "\n$fromStr: ${data.timeFrom}")
            }else
                builder.setMessage("$fromStr: ${data?.timeFrom}\n$toStr: ${data?.timeTo}")
        }else{
            builder.setTitle(
                Translate(context)
                    .getTranslatedString(R.array.CarNotInDB))
        }
        builder.setPositiveButton(
            Translate(context)
                .getTranslatedString(R.array.DialogOkeyBT)){ dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun responseErrorAlertWindow(){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getTranslatedString(R.array.ErrorResponseFromServer))
        builder.setPositiveButton(getTranslatedString(R.array.DialogOkeyBT)){dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun responsMobillyErrorWindow(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(lpnInput.text.toString())
        builder.setMessage(getTranslatedString(R.array.NoINFidParkDBMobillyError))
        builder.setPositiveButton(getTranslatedString(R.array.DialogOkeyBT)){dialog, which ->
            dialog.dismiss()
        }
        builder.setNeutralButton(getTranslatedString(R.array.DialogRepeatRequestBT)){dialog, which ->
            val client =
                Client(this, handler, null, null)
            client.sendRequestGetOne(lpnInput.text.toString())
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun getTranslatedString(stringID:Int):String{
        val index = getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE).getInt(
            R.string.LanguageID.toString(),0)
        val result = this.getResources().getStringArray(stringID)[index]
        return result
    }

}
