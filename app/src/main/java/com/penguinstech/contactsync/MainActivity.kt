package com.penguinstech.contactsync

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.Contacts
import com.penguinstech.contactsync.sync.SyncService
import java.util.*


class MainActivity : AppCompatActivity() {

    var roomDb: AppDatabase? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:Adapter
    private  lateinit var contactList:List<Contacts>
    private lateinit var mService: ContactService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to ContactService, cast the IBinder and get ContactService instance
            val binder = service as ContactService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        if (!checkIfUserIsLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
//            this.finish()
        }
        roomDb = AppDatabase.getDatabase(this)

        Intent(this, ContactService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        recyclerView = findViewById(R.id.mainRv)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contactList = ArrayList<Contacts>()
        askForContactPermission()
    }

    private fun checkIfUserIsLoggedIn(): Boolean {

        val sharedPref = this@MainActivity.getSharedPreferences(
            LoginActivity.appId, Context.MODE_PRIVATE)?: return false
        val userId = sharedPref.getString("phoneNo", "")

        return (userId!! != "")
    }

    private fun askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e(
                "TAG",
                " contacts permission " + (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_CONTACTS
                )
                        != PackageManager.PERMISSION_GRANTED).toString()
            )
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_CONTACTS
                )
                    != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.WRITE_CALL_LOG
                        ), 1001
                    )
                }
            } else {
                Log.e("TAG", "start main else")
                alreadyHasPermission()
            }
        } else {
            Log.e("TAG", "start main outer else")
            alreadyHasPermission()
        }
    }

    private fun alreadyHasPermission() {
        this.startService(Intent(this, ContactService::class.java))
        this.startService(Intent(this, SyncService::class.java))
        updateUi()
    }


    fun updateUi() {
        contactList = roomDb!!.ContactDataDao().all
        adapter = Adapter(this, contactList)
        recyclerView.adapter = adapter

    }



    override fun onDestroy() {
        super.onDestroy()
        WorkerHelper.scheduleWork(this, 1)
    }

    override fun onResume() {
        super.onResume()

        AppGlobals.deletedByMe = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    alreadyHasPermission()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(
                        this,
                        "Permission denied, please grant permission for the app to work.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.refresh -> {
                val time = 5
                Toast.makeText(this, "Refreshing please wait for $time seconds ...", Toast.LENGTH_LONG).show()
                if(mBound) {
                    mService.stopSelf()
                }
                this.startService(Intent(this, ContactService::class.java))
                Handler(Looper.getMainLooper()).postDelayed({

                    updateUi()

                }, ((time * 1000).toLong()))//10 seconds

            }
            R.id.friends-> {

                startActivity(Intent(this@MainActivity, FriendsActivity::class.java))
            }
            R.id.logout-> {
                //logout
                val sharedPref = this@MainActivity.getSharedPreferences(
                    LoginActivity.appId, Context.MODE_PRIVATE
                )
                val editor = sharedPref.edit()
                editor.putString("userId", "")
                editor.putString("userName", "")
                editor.putString("phoneNo","")
                editor.commit()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                this@MainActivity.finish()
            }
        }
        return false
    }

    override fun onStop() {
        super.onStop()

//        unbindService(connection)
        mBound = false
    }



}