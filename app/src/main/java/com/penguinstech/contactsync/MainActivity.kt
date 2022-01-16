package com.penguinstech.contactsync

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.Contacts

class MainActivity : AppCompatActivity() {

    var roomDb: AppDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roomDb = AppDatabase.getDatabase(this)
        askForContactPermission()
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
                                    Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CALL_LOG,
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
        updateUi()
    }


    private fun updateUi() {
        val contactList:List<Contacts> = roomDb!!.ContactDataDao().all
        val recyclerView: RecyclerView = findViewById(R.id.mainRv)
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        val adapter = Adapter(this, contactList)
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



}