package com.penguinstech.contactsync.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.penguinstech.contactsync.User
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.Contacts
import com.penguinstech.contactsync.room.Friend
import com.penguinstech.contactsync.room.SyncToken
import java.util.*

class SyncScheduler: BroadcastReceiver() {

    private var contactList:List<Contacts> = ArrayList<Contacts>()
    var roomDb: AppDatabase? = null
    var database:FirebaseDatabase ?= null
    override fun onReceive(context: Context?, intent: Intent?) {
//        android.os.Debug.waitForDebugger()
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {

            context!!.startService(Intent(context, SyncService::class.java))

        }else {
            roomDb = AppDatabase.getDatabase(context!!)
            FirebaseApp.initializeApp(context)
            database = FirebaseDatabase.getInstance()
            //get token
            val tokenList = roomDb!!.SyncTokenDao().getToken
            if (tokenList.isNotEmpty()) {
                //get last updated items
                val token:SyncToken = tokenList[0]
                contactList = roomDb!!.ContactDataDao().filterContactsByDate(token.lastSync)
                syncContacts()
                updateToken(token)

            }else {
                //get all items
                syncContacts()
                contactList = roomDb!!.ContactDataDao().allByDate
                updateToken(SyncToken())

            }


        }


    }

    private fun updateToken(token: SyncToken) {

        if (contactList.isNotEmpty()){
            if(token.lastSync == null) {

                //update token
                //so set token last sync to the timestamp of the last item added
                //the first item in list is the last item to be added in db,
                token.lastSync = contactList[0].updated_at
                roomDb!!.SyncTokenDao().insert(token)
            }else {

                //update token
                //so set token last sync to the timestamp of the last item added
                //the first item in list is the last item to be added in db,
                token.lastSync = contactList[0].updated_at
                roomDb!!.SyncTokenDao().updateData(token)
            }
        }
    }

    private fun syncContacts() {
        //check if contact is in firebase
        if (contactList.isNotEmpty()) {

            val ref = database!!.getReference("users")
            for (contact in contactList) {
                //if in firebase then add to friends
                val listener = object:ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {


                            val data: User = dataSnapshot.getValue(User::class.java) as User
//                            val data:HashMap<String, User> = dataSnapshot.value as HashMap<String, User>
                            if(data.userName != null) {
                                val friend = Friend()
                                friend.friend_id = contact.id.toString()
                                friend.mobile_no = contact.mobile_no
                                friend.name = contact.name

                                //check if user in room
                                Log.i("App User: ", " User Name: " + data!!.userName)
                                val count: Int = roomDb!!.FriendsDao().getFriendById(friend.friend_id)
                                Log.i("count", " : $count")
                                if (count > 0) {

                                    roomDb!!.FriendsDao().updateData(friend)
                                } else {
                                    roomDb!!.FriendsDao().insert(friend)
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.i("count", " : ${databaseError.message}")
                    }
                }
                ref.child(contact.mobile_no.toString()).addValueEventListener(listener)
            }
        }
    }


    fun setScheduler(context: Context) {


        //check if the scheduler has been set
        //if not set the scheduler
        if (!isSchedulerSet(context)) {
            //set up the alarm manager and the reference point ie pending intent
            //set repeating scheduler which repeats after 6 hours
            val midnight = Calendar.getInstance()
            midnight[Calendar.HOUR_OF_DAY] = 12
            midnight[Calendar.AM_PM] = Calendar.AM
            midnight[Calendar.MINUTE] = 0
            midnight[Calendar.SECOND] = 0
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(
                    context,
                    SyncScheduler::class.java
            )
            //set action so as the onReceive is triggered
            //the action should be the same as the declared action name in the manifest
            intent.action = "com.penguinstech.contactsync.scheduler"
            val pi = PendingIntent.getBroadcast(
                    context, 0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            ) //note flag_update_current which tells system how to handle new and existing pending intent
            am.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    midnight.timeInMillis, (1000 * 60 * 1).toLong(), pi
            ) // Millisec * Second * Minute  = 1 hour
        }
    }

    private fun isSchedulerSet(context: Context): Boolean {
        return PendingIntent.getBroadcast(
                context, 0,
                Intent(
                        context,
                        SyncScheduler::class.java
                ),
                PendingIntent.FLAG_NO_CREATE
        ) != null
    }
}