package com.example.fpkdv_kotlinstyle.utilit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import com.example.fpkdv_kotlinstyle.R
import com.example.fpkdv_kotlinstyle.Records.ZoneRecord
import com.example.fpkdv_kotlinstyle.Tools.Enums.whatStait
import kotlinx.android.synthetic.main.login_pass_layout.view.*

class LogIn(context:Activity,handler: Handler?) {
    val mainContext = context

    var localHandler:Handler? = null
    init{
        localHandler = handler
    }
    fun showDialog(){
        lateinit var dialog:AlertDialog
        val builder = AlertDialog.Builder(mainContext)
        builder.setTitle(Translate(mainContext).getTranslatedString(R.array.DialogChoseDB))
        val dataBasesStringArray = arrayOf("Spikeri nami", "Strong Volvo", "Web Biss")
        builder.setSingleChoiceItems(dataBasesStringArray,-1) { _, wich ->
            val settings = mainContext.getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE)
            var editor = settings.edit()
            when(wich){
                0->{
                    val res = mainContext.resources.getStringArray(R.array.SpikeriNami)
                    editor.putString(R.string.HostName.toString(), res[0])
                    editor.putString(R.string.Port.toString(),res[1])
                    editor.apply()
                }
                1->{
                    val res = mainContext.resources.getStringArray(R.array.VolvoStrong)
                    editor.putString(R.string.HostName.toString(), res[0])
                    editor.putString(R.string.Port.toString(),res[1])
                    editor.apply()
                }
                2 ->{
                    val res = mainContext.resources.getStringArray(R.array.FidparkWeb)
                    editor.putString(R.string.HostName.toString(), res[0])
                    editor.putString(R.string.Port.toString(),res[1])
                    editor.apply()
                }
            }
            dialog.dismiss()
            getLoginPass()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun getLoginPass(){
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(mainContext)
        val inflator = LayoutInflater.from(mainContext).inflate(R.layout.login_pass_layout,null)
        val settings = mainContext.getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
        inflator.PassWordDialogET.text =
            Editable.Factory.getInstance().newEditable(
            settings.getString(R.string.Password.toString(),""))
        inflator.UserNameDialogET.text =
            Editable.Factory.getInstance().newEditable(
                settings.getString(R.string.UserName.toString(),""))

        builder.setTitle("Enter some crap")
        builder.setView(inflator)

        builder.setPositiveButton("GO!"){dialog, which ->
            val settings = mainContext.getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
            val edit = settings.edit()
            edit.putString(R.string.UserName.toString(),inflator.UserNameDialogET.text.toString())
            edit.putString(R.string.Password.toString(),inflator.PassWordDialogET.text.toString())
            edit.apply()
            dialog.dismiss()
            localHandler?.sendMessage(Message.obtain(localHandler,whatStait.GetAllZonesRequest.ordinal))
        }
        builder.setNeutralButton("Cancel"){dialog, which ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Back"){dialog, which ->
            dialog.dismiss()
            showDialog()
        }
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
    }

    fun getZone(zoneList: MutableList<ZoneRecord>) {
        if (zoneList.size == 1){
            var setting = mainContext.getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE)
            var edit = setting.edit()
            edit.putString(R.string.ZoneID.toString(),zoneList[0].zoneID)
            edit.apply()
        }
        else if (zoneList.size>1){
            lateinit var dialog:AlertDialog
            val builder = AlertDialog.Builder(mainContext)
            builder.setTitle("Chose Zone")
            val zoneStringArray:MutableList<String> = mutableListOf()
            zoneList.forEach{
                zoneStringArray.add(it.zoneName)
            }
            builder.setSingleChoiceItems(zoneStringArray.toTypedArray(),-1) { _, wich ->
                val settings = mainContext.getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE)
                var editor = settings.edit()
                editor.putString(R.string.ZoneID.toString(),zoneList[wich].zoneID)
                editor.apply()
                dialog.dismiss()
            }
            dialog = builder.create()
            dialog.show()
        }
    }

    private fun output(data:String){
        Log.w("myLittleLog",data)
    }
}