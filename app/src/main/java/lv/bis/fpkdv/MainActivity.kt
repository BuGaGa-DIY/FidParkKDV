package lv.bis.fpkdv

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import lv.bis.fpkdv.R
import lv.bis.fpkdv.Tools.Client
import lv.bis.fpkdv.Tools.DataAdapret
import lv.bis.fpkdv.Records.DataRecord
import lv.bis.fpkdv.Records.ZoneRecord
import lv.bis.fpkdv.Tools.Enums.AdapterEnums
import lv.bis.fpkdv.Tools.Enums.whatStait
import lv.bis.fpkdv.utilit.FileLoger
import lv.bis.fpkdv.utilit.LogIn
import lv.bis.fpkdv.utilit.Translate
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.filter_dialog.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import java.util.*

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    val context:Activity = this

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navMenu: Menu

    lateinit var swipeRefresh: SwipeRefreshLayout
    var rowDataList: MutableList<DataRecord> = mutableListOf()
    var filteredDataList : MutableList<DataRecord> = mutableListOf()
    var zoneList: MutableList<ZoneRecord> = mutableListOf()
    lateinit var carListView:ListView
    lateinit var refreshButton: Button
    lateinit var sortNameBT: Button
    lateinit var sortTimeBT: Button
    var dataListAdapter: DataAdapret? = null

    var myHandler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        navMenu = navView.menu

        carListView = findViewById(R.id.ListOfItems)
        dataListAdapter =
            DataAdapret(context, filteredDataList)
        carListView.adapter = dataListAdapter

        refreshButton = findViewById(R.id.refreshBT)
        refreshButton.setOnClickListener{
            swipeRefresh.isRefreshing = true
            val client = Client(
                context,
                myHandler,
                rowDataList,
                zoneList
            )
            client.sendRequestGetAll()
        }
        swipeRefresh = findViewById(R.id.swipeContainer)
        swipeRefresh.setOnRefreshListener{
            val client = Client(
                context,
                myHandler,
                rowDataList,
                zoneList
            )
            client.sendRequestGetAll()
        }

        sortNameBT = findViewById(R.id.sortByName)
        sortTimeBT = findViewById(R.id.sortByTime)

        sortTimeBT.setOnClickListener {
            if (sortTimeBT.text.equals("T<")){
                dataListAdapter?.sortByTime(AdapterEnums.TimeHighToLow.ordinal)
                sortTimeBT.text = "T>"
            }
            else{
                dataListAdapter?.sortByTime(AdapterEnums.TimeLowToHigh.ordinal)
                sortTimeBT.text = "T<"
            }
            dataListAdapter?.notifyDataSetChanged()
        }

        sortNameBT.setOnClickListener {
            if (sortNameBT.text.equals("A-Z")){
                dataListAdapter?.sortByName(AdapterEnums.AToZ.ordinal)
                sortNameBT.text = "Z-A"
            }else{
                dataListAdapter?.sortByName(AdapterEnums.ZToA.ordinal)
                sortNameBT.text = "A-Z"
            }
            dataListAdapter?.notifyDataSetChanged()
        }
        myHandler = @SuppressLint("HandlerLeak")
        object : Handler(){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what){
                    whatStait.RequestFail.ordinal -> {
                        Toast.makeText(
                            context,
                            Translate(context)
                                .getTranslatedString(R.array.ConnectionFail),
                            Toast.LENGTH_SHORT
                        ).show()
                        swipeRefresh.isRefreshing = false
                    }
                    whatStait.GetAllReady.ordinal -> {
                        filteredDataList.clear()
                        filteredDataList.addAll(setFilter())
                        FileLoger(applicationContext).WriteLine("Data size after filter: ${filteredDataList.size}")
                        dataListAdapter?.notifyDataSetChanged()
                        swipeRefresh.isRefreshing = false
                        if (msg.obj == false) getMobillyResponseError()
                    }
                    whatStait.GetAllZonesRequest.ordinal -> {
                        val client = Client(
                            context,
                            myHandler,
                            rowDataList,
                            zoneList
                        )
                        client.sendRequestGetZones()
                    }
                    whatStait.GetZonesReady.ordinal -> {
                        val loadZone =
                            LogIn(context, myHandler)
                        loadZone.getZone(zoneList)
                        navView.getHeaderView(0).DBTextView.text =
                            "DB: ${getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE).
                            getString(R.string.DBName.toString(),"")}"
                    }
                    whatStait.ZonePicked.ordinal -> {
                        val client = Client(
                            context,
                            myHandler,
                            rowDataList,
                            zoneList
                        )
                        client.sendRequestGetAll()
                    }
                    whatStait.AuthorizationFail.ordinal -> LogIn(
                        context,
                        myHandler
                    ).getLoginPass(true)
                    else -> swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun getMobillyResponseError(){
        val builder = AlertDialog.Builder(this)
        val msg = Translate(context).getTranslatedString(R.array.ErrorResponseFromMobilly)
        builder.setMessage(msg)
        builder.setPositiveButton(Translate(context).getTranslatedString(R.array.DialogOkeyBT))
        {dialog, which ->
            dialog.dismiss()
        }
        builder.setNeutralButton(Translate(context).getTranslatedString(R.array.DialogRepeatRequestBT))
        {dialog, which ->
            val client = Client(
                context,
                myHandler,
                rowDataList,
                zoneList
            )
            client.sendRequestGetAll()
            dialog.dismiss()
            swipeRefresh.isRefreshing = true
        }
        builder.show()
    }
    private fun setFilter():MutableList<DataRecord>{
        var tmpDataList = mutableListOf<DataRecord>()
        val setting = getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)

        for (item in rowDataList){
            if (item.timeLeftInMillis in 0..15) tmpDataList.add(item)
            else if (setting.getBoolean(R.string.FilterTimeEnough.toString(),true) && item.timeLeftInMillis > 15) tmpDataList.add(item)
            else if (setting.getBoolean(R.string.FilterTimeOver.toString(),true) && item.timeLeftInMillis < 0) tmpDataList.add(item)
            else if (setting.getBoolean(R.string.FilterMobilly.toString(),true) && item.parkingType == "Mobilly") tmpDataList.add(item)
            else if (setting.getBoolean(R.string.FilterAbonnement.toString(),true) && item.parkingType == "subscription - admin") tmpDataList.add(item)
        }

        return tmpDataList
    }

    private fun output(data:String){
        Log.w("myLittleLog",data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu?.findItem(R.id.search)?.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        val searchItem = menu.findItem(R.id.search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null){
                    filteredDataList.clear()
                    setFilter()
                }else {
                    filteredDataList.clear()
                    filteredDataList.addAll(setFilter().filter{ it.lpn.contains(newText.toUpperCase(Locale.ROOT).toRegex()) })
                    dataListAdapter?.notifyDataSetChanged()
                }
                return true
            }

        })

        return true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.nav_check_one_lpn -> {
                val intent = Intent(context,
                    CheckOneLpnActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_LoginBT ->{
              val login = LogIn(context, myHandler)
                login.showDialog()
            }
            R.id.nav_FilterBT ->{
                val setting = getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
                var edit = setting.edit()
                val builder = AlertDialog.Builder(context)
                val inflator = LayoutInflater.from(context).inflate(R.layout.filter_dialog,null)
                inflator.checkbox_A.isChecked = setting.getBoolean(R.string.FilterAbonnement.toString(),true)
                inflator.checkbox_M.isChecked = setting.getBoolean(R.string.FilterMobilly.toString(),true)
                inflator.checkbox_TimeEnough.isChecked = setting.getBoolean(R.string.FilterTimeEnough.toString(),true)
                inflator.checkbox_TimeEnough.text = Translate(
                    context
                )
                    .getTranslatedString(R.array.FilterTimeEnough)
                inflator.checkbox_timeEnd.isChecked = setting.getBoolean(R.string.FilterTimeOver.toString(),true)
                inflator.checkbox_timeEnd.text = Translate(
                    context
                ).getTranslatedString(R.array.FilterTimeOver)
                inflator.checkbox_A.text = Translate(
                    context
                )
                    .getTranslatedString(R.array.DialogSubscription)
                inflator.FilterDialogTitle.text = Translate(
                    context
                ).getTranslatedString(R.array.NavFilterBT)
                //if (setting.getString(R.string.HostName.toString(),"") == resources.getStringArray(R.array.VolvoStrong)[0]) inflator.checkbox_M.isVisible = false
                builder.setView(inflator)
                builder.setPositiveButton(
                    Translate(
                        context
                    )
                        .getTranslatedString(R.array.DialogOkeyBT)){ dialog, which ->
                    edit.putBoolean(R.string.FilterAbonnement.toString(),inflator.checkbox_A.isChecked)
                    edit.putBoolean(R.string.FilterMobilly.toString(),inflator.checkbox_M.isChecked)
                    edit.putBoolean(R.string.FilterTimeEnough.toString(),inflator.checkbox_TimeEnough.isChecked)
                    edit.putBoolean(R.string.FilterTimeOver.toString(),inflator.checkbox_timeEnd.isChecked)
                    edit.apply()
                    filteredDataList.clear()
                    filteredDataList.addAll(setFilter())
                    dataListAdapter?.notifyDataSetChanged()
                    dialog.dismiss()
                }

                builder.create().show()
            }
            R.id.nav_LogFiles ->{
                val intent = Intent(applicationContext,
                    LogFileList::class.java)
                startActivity(intent)
            }
            R.id.nav_ExitBT -> finish()
            R.id.nav_LanguageEN -> {
                val setting = getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
                val edit = setting.edit()
                edit.putInt(R.string.LanguageID.toString(),0)
                edit.apply()
                initUI()
            }
            R.id.nav_LanguageRU -> {
                val setting = getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
                val edit = setting.edit()
                edit.putInt(R.string.LanguageID.toString(),1)
                edit.apply()
                initUI()
            }
            R.id.nav_LanguageLV -> {
                val setting = getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE)
                val edit = setting.edit()
                edit.putInt(R.string.LanguageID.toString(),2)
                edit.apply()
                initUI()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val client =
            Client(context, myHandler, rowDataList, null)
        client.sendRequestGetAll()
        initUI()
        navView.getHeaderView(0).DBTextView.text =
            "DB: ${getSharedPreferences(R.string.PreferenceName.toString(),Context.MODE_PRIVATE).
            getString(R.string.DBName.toString(),"")}"
    }

    private fun initUI(){
        refreshButton.text = Translate(context)
            .getTranslatedString(R.array.RefreshBT)
        navMenu.findItem(R.id.nav_ExitBT).title = Translate(
            context
        ).getTranslatedString(R.array.NavExitBT)
        navMenu.findItem(R.id.nav_FilterBT).title = Translate(
            context
        ).getTranslatedString(R.array.NavFilterBT)
        navMenu.findItem(R.id.nav_check_one_lpn).title = Translate(
            context
        ).getTranslatedString(R.array.NavCheckOneLpnBT)
        navMenu.findItem(R.id.nav_LoginBT).title = Translate(
            context
        ).getTranslatedString(R.array.NavChangeDbBT)

    }

}
