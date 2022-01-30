package com.penguinstech.contactsync

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.ContUPDt
import com.penguinstech.contactsync.room.Contacts
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.DriverManager.println
import java.util.*
import kotlin.collections.HashMap


class ContactService : Service() {
    private lateinit var prefManager: PrefManager
    private lateinit var arrayList: ArrayList<HashMap<String, String>>
    private lateinit var contObserver: ContObserver
    private lateinit var roomDB: AppDatabase
    private val binder = LocalBinder()


    override fun onBind(intent: Intent): IBinder? {
//        TODO("Return the communication channel to the service.")

        return binder
    }

    override fun onCreate() {
        super.onCreate()
//        android.os.Debug.waitForDebugger()
        prefManager = PrefManager(this)
        arrayList = ArrayList()
        roomDB = AppDatabase.getDatabase(this)
        alreadyHasPermission()
        showToast("Contact sync will be completed in the next few minutes.")

    }

    private fun showToast(msg: String) {


        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    }

    private fun alreadyHasPermission(){

        val db = AppDatabase.getDatabase(this)
//        if (prefManager.isFirstTimeLaunch) {

        GlobalScope.launch{
            val cr = contentResolver
            val cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            var dataHashMap: HashMap<String, String>
            Log.e("TAG", "reading started for " + cur!!.count)
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    dataHashMap = HashMap()
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val rawId =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                    dataHashMap.put("id", id)
                    dataHashMap.put("raw_id", rawId)
                    val lookupKey =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY))
                    dataHashMap.put("look_up_key", lookupKey)
                    val name =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    dataHashMap.put("name", name)
                    if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt() > 0
                    ) {
                        // get the phone number
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        Log.e("TAG", "starting loop for " + cur.position + " getting number")
                        while (pCur!!.moveToNext()) {
                            val phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            val type = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                            )
                            Log.e("TAG", "ending loop for " + cur.position)
                            if (type != null && type == "0" && phone != null) {
                                dataHashMap.put("custom", phone)
                            }
                            if (type != null && type == "1" && phone != null) {
                                dataHashMap.put("home_phone", phone)
                            }
                            if (type != null && type == "2" && phone != null) {
                                dataHashMap.put("mobile", phone)
                            }
                            if (type != null && type == "7" && phone != null) {
                                dataHashMap.put("other", phone)
                            }
                            if (type != null && type == "3" && phone != null) {
                                dataHashMap.put("work", phone)
                            }
                        }
                        Log.e(
                            "TAG",
                            "starting loop for " + cur.position + " getting number end"
                        )
                        pCur.close()
                        println("here $dataHashMap")
                        //                           if (prefManager.isFirstTimeLaunch()) {
                        //                           }

                    }
                    arrayList.add(dataHashMap)
                }
            }
            cur.close()
            if (arrayList.size > 1) {
                prefManager.isFirstTimeLaunch = false
            }

//                    if (!::adapter.isInitialized) {
//                        adapter = Adapter(this, arrayList)
//                        contacts.setAdapter(adapter)
//                    }
//                    adapter.notifyDataSetChanged()
            processData()


        }
//        }
        contObserver = ContObserver(
            Handler(Looper.getMainLooper()), this
        )
        startContactObserver()

    }




    private fun processData() {
        val data: HashMap<String, HashMap<String,String>> = HashMap()
        GlobalScope.launch {
            val cr =contentResolver
            val cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            if (cur!!.count > 0) {
                prefManager.count = cur.count
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    //                if (this.id.equals(id)) {
                    while (cur1!!.moveToNext()) {
                        //to get the contact names
                        val emailType =
                            cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                        Log.e("Name :", "type $emailType")
                        //                            if (id.equals(ShowContact.this.id)) {
                        val email =
                            cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                        Log.e("Email", email!!)
                        if (emailType != null && emailType == "1" && email != null && email.trim { it <= ' ' }
                                .isNotEmpty()) {
                            if (data.containsKey(id)) {
                                val innerMap: HashMap<String, String> = data[id] as HashMap<String, String>
                                innerMap.put("home_email", email)
                            } else {
                                val hashMap = HashMap<String, String>()
                                hashMap.put("home_email", email)
                                data.put(id, hashMap)
                            }
                        }
                        if (emailType != null && emailType == "2" && email != null && email.trim { it <= ' ' }
                                .isNotEmpty()) {
                            if (data.containsKey(id)) {
                                val innerMap: HashMap<String, String> = data[id] as HashMap<String, String>
                                innerMap.put("work_email", email)
                            } else {
                                val hashMap = HashMap<String, String>()
                                hashMap.put("work_email", email)
                                data.put(id, hashMap)
                            }
                        }
                        if (emailType != null && emailType == "3" && email != null && email.trim { it <= ' ' }
                                .isNotEmpty()) {
                            if (data.containsKey(id)) {
                                val innerMap: HashMap<String, String> = data[id] as HashMap<String, String>
                                innerMap.put("other_email", email)
                            } else {
                                val hashMap = HashMap<String, String>()
                                hashMap.put("other_email", email)
                                data.put(id, hashMap)
                            }
                        }
                        if (emailType == null && email != null && email.trim { it <= ' ' }.isNotEmpty()) {
                            if (data.containsKey(id)) {
                                val innerMap: HashMap<String, String> = data[id] as HashMap<String, String>
                                innerMap.put("custom_email", email)
                            } else {
                                val hashMap = HashMap<String, String>()
                                hashMap.put("custom_email", email)
                                data.put(id, hashMap)
                            }
                        }
                        //                            }
                    }
                    cur1.close()
                }
                saveInDB(data)
//                displayContacts()
            } else {
                saveInDB(data)
//                displayContacts()
            }
        }
    }

    //Contact Sync
    private fun startContactObserver() {
        try {
            //Registering contact observer
            this.contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true, (contObserver)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveInDB(hashMap: HashMap<String, HashMap<String,String>>) {
        Log.i("key", hashMap.keys.toString())
        GlobalScope.launch {
            for (i in arrayList.indices) {
                val data: java.util.HashMap<String, String> = arrayList[i]
                val mainData = Contacts()
                mainData.id = java.lang.String.valueOf(data.get("id")).toInt()
                mainData.rawId = java.lang.String.valueOf(data.get("raw_id"))
                mainData.name = java.lang.String.valueOf(data.get("name"))
                val uri = getPhotoUri(this@ContactService, data["id"].toString().toLong())
                mainData.imageUri = (uri.toString())
                mainData.lookupKey = (data["look_up_key"].toString())
                if (data.containsKey("home_phone")) {
                    mainData.home_no = java.lang.String.valueOf(data["home_phone"])
                } else {
                    mainData.home_no = ""
                }
                if (data.containsKey("mobile")) {
                    mainData.mobile_no = java.lang.String.valueOf(data["mobile"])
                } else {
                    mainData.mobile_no = ""
                }
                if (data.containsKey("custom")) {
                    mainData.custom_no = java.lang.String.valueOf(data["custom"])
                } else {
                    mainData.custom_no = ""
                }
                if (data.containsKey("other")) {
                    mainData.other_no = java.lang.String.valueOf(data["other"])
                } else {
                    mainData.other_no = ""
                }
                if (data.containsKey("work")) {
                    mainData.work_no = java.lang.String.valueOf(data.get("work"))
                } else {
                    mainData.work_no = ""
                }
                if (hashMap.containsKey(java.lang.String.valueOf(mainData.id))) {
                    val mailMap: HashMap<String, String> = hashMap[(mainData.id).toString()]!!
                    if (mailMap.containsKey("home_email")) {
                        mainData.personal_email = (mailMap["home_email"].toString())
                    } else {
                        mainData.personal_email = ""
                    }
                    if (mailMap.containsKey("custom_email")) {
                        mainData.custom_email =
                            java.lang.String.valueOf(mailMap.get("custom_email"))
                    } else {
                        mainData.custom_email = ""
                    }
                    if (mailMap.containsKey("work_email")) {
                        mainData.work_email = java.lang.String.valueOf(mailMap.get("work_email"))
                    } else {
                        mainData.work_email = ""
                    }
                    if (mailMap.containsKey("other_email")) {
                        mainData.other_email = java.lang.String.valueOf(mailMap.get("other_email"))
                    } else {
                        mainData.other_email = ""
                    }
                }
                Log.e("TAG", " id " + mainData.id + "  number" + mainData.mobile_no)
                roomDB.ContactDataDao().insert(mainData)
                if(roomDB.ContUpdateDao().notiExists(mainData.id) == 0){
                    roomDB.ContUpdateDao().addcontUpdate(
                        ContUPDt(
                            0, "System",
                            mainData.id,
                            Calendar.getInstance().timeInMillis,
                            "Contact created.",
                            null,
                            "Notification",
                        )
                    )
                    //    roomDB.ContactDataDao().updateNewContact(mainData.id,"New")
                }
            }
        }
    }

    //getPhotoUri
    fun getPhotoUri(context: Context?, contactId: Long): Uri? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
    }


    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): ContactService = this@ContactService
    }

}