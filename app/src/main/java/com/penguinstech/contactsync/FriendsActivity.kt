package com.penguinstech.contactsync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.Contacts
import com.penguinstech.contactsync.room.Friend
import com.penguinstech.contactsync.sync.FriendsAdapter
import java.util.*

class FriendsActivity : AppCompatActivity() {

    var roomDb: AppDatabase? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:FriendsAdapter
    private  lateinit var friendList:List<Friend>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        roomDb = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.friendsRv)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        friendList = ArrayList<Friend>()

        updateUi()
    }

    fun updateUi() {
        friendList = roomDb!!.FriendsDao().getFriends
        //check if background sync has occurred
        if (roomDb!!.SyncTokenDao().getToken.isNotEmpty()) {
            //check if there is data
            if (friendList.isEmpty()) {
                Snackbar.make(recyclerView, "0 results found", Snackbar.LENGTH_LONG).show()
            }else {

                adapter = FriendsAdapter(this, friendList)
                recyclerView.adapter = adapter
            }

        }else {
            Snackbar.make(recyclerView, "Waiting for sync to start, please be patient...", Snackbar.LENGTH_LONG).show()
        }

    }
}