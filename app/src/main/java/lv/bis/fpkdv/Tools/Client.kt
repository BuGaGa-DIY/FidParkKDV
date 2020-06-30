package lv.bis.fpkdv.Tools

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.Log
import lv.bis.fpkdv.R
import lv.bis.fpkdv.Records.DataRecord
import lv.bis.fpkdv.Records.ZoneRecord
import lv.bis.fpkdv.Tools.Enums.whatStait
import lv.bis.fpkdv.utilit.FileLoger
import okhttp3.*
import java.io.IOException

open class Client(val context:Activity, handler: Handler,
                  private var dataList: MutableList<DataRecord>?,
                  private var zoneList: MutableList<ZoneRecord>?
) : AsyncTask<Void, Void, Void>() {


    var RequestLink:String = ""
    var okHttpClient: OkHttpClient = OkHttpClient()

    private val myHandler = handler
    private var response:String? = null

    private val settings = context.getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE)
    private var _RequestType = ""



    fun sendRequestGetAll(){
        val _host = settings.getString(R.string.HostName.toString(),"http://fidparkweb.bis.lv")
        val _port = settings.getString(R.string.Port.toString(),"80")
        val _username = settings.getString(R.string.UserName.toString(),"volvo1")
        val _pass = settings.getString(R.string.Password.toString(),"volvo1")
        val _zoneid = settings.getString(R.string.ZoneID.toString(),"5")
        RequestLink = "$_host:$_port/Service.asmx/GetPaidLPNsByZoneUsernameAuth?" +
                "ZoneID=$_zoneid&Username=$_username&Password=$_pass"
        _RequestType = "All"
        execute()
    }

    fun sendRequestGetZones(){
        val _host = settings.getString(R.string.HostName.toString(),"http://fidparkweb.bis.lv")
        val _port = settings.getString(R.string.Port.toString(),"80")
        val _username = settings.getString(R.string.UserName.toString(),"volvo1")
        val _pass = settings.getString(R.string.Password.toString(),"volvo1")
        RequestLink = "$_host:$_port/Service.asmx/GetAllZonesUsernameAuth?" +
                "Username=$_username&Password=$_pass"
        _RequestType = "Zones"
        execute()
    }

    fun sendRequestGetOne(lpn:String){
        val _host = settings.getString(R.string.HostName.toString(),"http://fidparkweb.bis.lv")
        val _port = settings.getString(R.string.Port.toString(),"80")
        val _username = settings.getString(R.string.UserName.toString(),"volvo1")
        val _pass = settings.getString(R.string.Password.toString(),"volvo1")
        val _zoneid = settings.getString(R.string.ZoneID.toString(),"5")
        RequestLink = "$_host:$_port/Service.asmx/CheckIsLPNPaidUsernameAuth?" +
                "LPN=$lpn&ZoneID=$_zoneid&Username=$_username&Password=$_pass"
        _RequestType = "One"
        execute()
    }
    override fun doInBackground(vararg params: Void?): Void? {
        //output(R.string.PreferenceName.toString())
        val request: Request = Request.Builder().url(RequestLink).build()
        FileLoger(context).WriteLine("$_RequestType Request sent")
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                sendHandlerMsg(whatStait.RequestFail.ordinal,e)
                output("Request error: $e")
                FileLoger(context).WriteLine("Request fail: ${e?.printStackTrace()}")
            }

            override fun onResponse(call: Call?, response: Response?) {
                this@Client.response = response?.body()?.string()
                FileLoger(context).WriteLine("Client response: ${this@Client.response}")
                output("response:${this@Client.response}")
                parsDataSender(this@Client.response)
            }
        })
        return null
    }

    private fun parsDataSender(data:String?){
        var inputdata = ""
        if (data != null) inputdata = data
        else {
            sendHandlerMsg(whatStait.GotNullRespons.ordinal,null)
            return
        }
        if (inputdata.contains("You are not authorized to use this section.")){
            FileLoger(context).WriteLine("Got incorrect Login Pass response")
            sendHandlerMsg(whatStait.AuthorizationFail.ordinal,null)
            return
        }
        var _ErrorFlaf = false
        when(_RequestType){
            "All" -> {
                if (data.contains("</GetPaidLPNsByZoneUsernameAuth>")) parsGetAllData(inputdata)
                else _ErrorFlaf = true
            }
            "Zones" -> {
                if (data.contains("</GetAllZonesUsernameAuth>")) parsGetZonesData(inputdata)
                else _ErrorFlaf = true
            }
            "One" -> {
                if (data.contains("</CheckIsLPNPaidUsernameAuth>")) parsGetOneData(inputdata)
                else _ErrorFlaf = true
            }
            else -> output("Parsing sender request type missmatch")
        }
        if (_ErrorFlaf) sendHandlerMsg(whatStait.UnexpectedPackage.ordinal,inputdata)

    }


    private fun parsGetAllData(data: String){
        var tmpData:String = data
        dataList?.clear()
        val tmpResponseCount = tmpData.substring(
            tmpData.indexOf("<Response>")+10
            ,tmpData.indexOf("</Response>"))
        FileLoger(context).WriteLine("Response in row data: ${tmpResponseCount}")
        while (tmpData.contains("<Result>")) {
            var startIndex = tmpData.indexOf("<LPN>")
            var stopIndex = tmpData.indexOf("</LPN>")
            val tmpLpn = tmpData.substring(startIndex + 5, stopIndex)
            startIndex = tmpData.indexOf("<From>")
            stopIndex = tmpData.indexOf("</From>")
            val tmpFrom = tmpData.substring(startIndex + 6, stopIndex)
            startIndex = tmpData.indexOf("<To>")
            stopIndex = tmpData.indexOf("</To>")
            val tmpTo = tmpData.substring(startIndex + 4, stopIndex)
            startIndex = tmpData.indexOf("<ParkingType>")
            stopIndex = tmpData.indexOf("</ParkingType>")
            val tmpParkingType = tmpData.substring(startIndex + 13, stopIndex)
            dataList?.add(
                DataRecord(
                    tmpLpn,
                    tmpFrom,
                    tmpTo,
                    tmpParkingType
                )
            )
            tmpData = tmpData.substring(tmpData.indexOf("</Result>")+ 9)
        }
        sendHandlerMsg(whatStait.GetAllReady.ordinal,null)
    }

    private fun parsGetZonesData(data: String){
        var tmpData = data
        zoneList?.clear()
        while (tmpData.contains("<Zone>")){
            var startIndex = tmpData.indexOf("<ZoneID>")
            var stopIndex = tmpData.indexOf("</ZoneID>")
            val tmpZoneid = tmpData.substring(startIndex+8,stopIndex)
            startIndex = tmpData.indexOf("<ZoneName>")
            stopIndex = tmpData.indexOf("</ZoneName>")
            val tmpZonename = tmpData.substring(startIndex + 10, stopIndex)
            zoneList?.add(
                ZoneRecord(
                    tmpZonename,
                    tmpZoneid
                )
            )
            tmpData = tmpData.substring(tmpData.indexOf("</Zone>") + 7)
        }
        sendHandlerMsg(whatStait.GetZonesReady.ordinal,null)
    }

    private fun parsGetOneData(data: String){
        var tmpData = data
        if(tmpData.contains("<Response>False</Response>")) sendHandlerMsg(whatStait.GetOneReadyFalse.ordinal,null)
        else {
            var startIndex = tmpData.indexOf("<LPN>")
            var stopIndex = tmpData.indexOf("</LPN>")
            val tmpLpn = tmpData.substring(startIndex + 5, stopIndex)
            startIndex = tmpData.indexOf("<From>")
            stopIndex = tmpData.indexOf("</From>")
            val tmpFrom = tmpData.substring(startIndex + 6, stopIndex)
            startIndex = tmpData.indexOf("<To>")
            stopIndex = tmpData.indexOf("</To>")
            val tmpTo = tmpData.substring(startIndex + 4, stopIndex)
            startIndex = tmpData.indexOf("<ParkingType>")
            stopIndex = tmpData.indexOf("</ParkingType>")
            val tmpParkingType = tmpData.substring(startIndex + 13, stopIndex)
            val singeDataRecord = DataRecord(
                tmpLpn,
                tmpFrom,
                tmpTo,
                tmpParkingType
            )
            sendHandlerMsg(whatStait.GetOneReadyTrue.ordinal,singeDataRecord)
        }
    }

    private fun sendHandlerMsg(what:Int, obj:Any?){
        if (obj == null) myHandler.sendMessage(Message.obtain(myHandler,what))
        else myHandler.sendMessage(Message.obtain(myHandler,what,obj))
    }
    private fun output(data:String){
        Log.w("myLittleLog",data)
    }
}