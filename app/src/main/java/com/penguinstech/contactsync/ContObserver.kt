package com.penguinstech.contactsync

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.penguinstech.contactsync.room.AppDatabase
import com.penguinstech.contactsync.room.Contacts
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class ContObserver (handler: Handler?, var context: Context) : ContentObserver(handler) {

    private var lastUpdated: Long = 0
    private var prefManager: PrefManager? = null
//    private var prefManager: PrefManager? = null
    var arrayList: ArrayList<HashMap<*, *>>? = null
    private var isFirst=true
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        val cr = context.contentResolver
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val sharedPref=context.getSharedPreferences("app", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("isFirst",true)){
            contactAdded(selfChange)
            sharedPref.edit().putInt("totalContacts",cur!!.count).apply()
            sharedPref.edit().putBoolean("isFirst",false).apply()
        }else
        {

            if (cur!!.count >= sharedPref.getInt("totalContacts",0)) {

                contactAdded(selfChange)
            }
            sharedPref.edit().putInt("totalContacts",cur!!.count).apply()

        }


//        val contactsList: List<Contacts> = AppDatabase.getDatabase(
//            context
//        ).ContactDataDao().all

        Log.e("TAG", "change happened$selfChange")
        Log.e(
            "TAG",
            " cursor list " + cur!!.count + " saved list " + sharedPref.getInt("totalContacts",0)
        )
//        Log.e(
//            "TAG",
//            " contactList " + contactsList!!.size + " room " + cur!!.count
//        )
//        if(!isFirst) {
//            if (contactsList.size <= cur!!.count)
//                contactAdded(selfChange)
//        }else
//        {
//            isFirst=false
//            contactAdded(selfChange)
//        }
    }

    @Synchronized
    fun contactAdded(selfChange: Boolean) {
        if (!selfChange) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val diff = System.currentTimeMillis() - lastUpdated
                    Log.i("time ", "lastUpdated: $lastUpdated")
                    Log.i("time ", "diff: $diff")
                    if (TimeUnit.MILLISECONDS.toSeconds(diff) > 3) {
                        lastUpdated = System.currentTimeMillis()
                        prefManager = PrefManager(context)
                        var newData = false
                        val contactsList: List<Contacts> = AppDatabase.getDatabase(
                            context
                        ).ContactDataDao().all
                        val cr = context.contentResolver
                        val cursor = cr.query(
                            ContactsContract.Contacts.CONTENT_URI,
                            null,
                            null,
                            null,
                            ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + " ASC"
                        )
                        Log.e(
                            "TAG",
                            " cursor " + prefManager!!.count + " room " + cursor!!.count
                        )
                        if (cursor.count < prefManager!!.count) {
//                            if (!AppGlobals.deletedByMe) {
//                                arrayList = ArrayList()
//                                AppDatabase.getDatabase(
//                                    context
//                                ).clearAllTables()
//                                processDelete(context)
//                            }
                        } else if (cursor.count > prefManager!!.count) {
                            val addedNewCursor = cr.query(
                                ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null
                            )
                            addedNewCursor!!.moveToLast()
                            var contactName: String? = null
                            val photo: String? = null
                            var contactNumber: String? = null
                            val id = addedNewCursor.getString(
                                addedNewCursor.getColumnIndex(ContactsContract.Contacts._ID)
                            )
                            val rawId = addedNewCursor.getString(
                                addedNewCursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
                            )
                            if (addedNewCursor.getString(
                                    addedNewCursor.getColumnIndex(
                                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                                    )
                                ).toInt() > 0
                            ) {
                                val pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id),
                                    null
                                )
                                if (pCur != null) {
                                    pCur.moveToFirst()
                                    val mainData = Contacts()
                                    contactNumber =
                                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    contactName =
                                        pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                                    Log.i("TAG", " contact $contactNumber")
                                    //here you will get your contact information
                                    // email code starts here
                                    mainData.name = contactName
                                    mainData.rawId = rawId
                                    val numberCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        arrayOf(id),
                                        null
                                    )
                                    Log.i("TAG", " numbersssssssss " + numberCur!!.count)
                                    while (numberCur.moveToNext()) {
                                        val phone = numberCur.getString(
                                            numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        )
                                        val type = numberCur.getString(
                                            numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                        )
                                        Log.e("TAG", " phone  $phone type $type")
                                        Log.e("TAG", " type  " + (type != null).toString())
                                        Log.e("TAG", " type  " + (type == "2").toString())
                                        Log.e("TAG", " type  " + (phone != null).toString())
                                        if (type != null && type == "0" && phone != null) {
                                            mainData.custom_no = phone
                                        }
                                        if (type != null && type == "1" && phone != null) {
                                            mainData.home_no = phone
                                        }
                                        if (type != null && type == "2" && phone != null) {
                                            mainData.mobile_no = phone
                                        }
                                        if (type != null && type == "7" && phone != null) {
                                            mainData.other_no = phone
                                        }
                                        if (type != null && type == "3" && phone != null) {
                                            mainData.work_no = phone
                                        }
                                    }
                                    pCur.close()
                                    addedNewCursor.close()
                                    Log.e("TAG", id + " THTTTTTTTTTT " + mainData.mobile_no)
                                    mainData.id = id.toInt()
                                    AppDatabase.getDatabase(
                                        context
                                    ).ContactDataDao()
                                        .insert(mainData)
                                    Log.e("TAG", id + " check id " + mainData.id)
                                    prefManager!!.count = (prefManager!!.count + 1)
                                    handleEmails(context.applicationContext, id)
                                }
                            }
                        } else {
                            if (cursor != null && cursor.count > 0) {
                                //moving cursor to last position
                                //to get last element added
                                if (!AppGlobals.deletedByMe) {
                                    cursor.moveToLast()
                                    var contactName: String? = null
                                    val photo: String? = null
                                    var contactNumber: String? = null
                                    val id =
                                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                                    val rawId =
                                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                                    if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                            .toInt() > 0
                                    ) {
                                        val pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            arrayOf(id),
                                            null
                                        )
                                        if (pCur != null) {
                                            pCur.moveToFirst()
                                            var contactsData: Contacts = AppDatabase.getDatabase(
                                                context
                                            ).ContactDataDao().getContactInfo(id)
                                            if (contactsData == null) {
                                                newData = true
                                                contactsData = Contacts()
                                            }
                                            contactsData.id = id.toInt()
                                            contactsData.rawId = rawId
                                            contactNumber =
                                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            contactName =
                                                pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                                            Log.i("TAG", " contact number$contactName")
                                            //here you will get your contact information
                                            // email code starts here
                                            contactsData.name = contactName
                                            val numberCur = cr.query(
                                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                null,
                                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                                arrayOf(id),
                                                null
                                            )
                                            while (numberCur!!.moveToNext()) {
                                                val phone = numberCur.getString(
                                                    numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                                )
                                                val type = numberCur.getString(
                                                    numberCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                                                )
                                                if (type != null && type == "0" && phone != null) {
                                                    contactsData.custom_no = phone
                                                }
                                                if (type != null && type == "1" && phone != null) {
                                                    contactsData.home_no = phone
                                                }
                                                if (type != null && type == "2" && phone != null) {
                                                    contactsData.mobile_no = phone
                                                }
                                                if (type != null && type == "7" && phone != null) {
                                                    contactsData.other_no = phone
                                                }
                                                if (type != null && type == "3" && phone != null) {
                                                    contactsData.work_no = phone
                                                }
                                            }
                                            if (AppDatabase.getDatabase(
                                                    context
                                                ).ContactDataDao()
                                                    .getContactInfo(contactsData.id.toString()) != null
                                            ) {
                                                AppDatabase.getDatabase(
                                                    context
                                                ).ContactDataDao()
                                                    .update(contactsData)
                                            } else {
                                                AppDatabase.getDatabase(
                                                    context
                                                ).ContactDataDao()
                                                    .insert(contactsData)
                                            }
                                            handleEmails(context.applicationContext, id)
                                            pCur.close()
                                        }
                                        pCur!!.close()
                                    }
                                    cursor.close()
                                } else {
                                    lastUpdated = System.currentTimeMillis()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        AppGlobals.deletedByMe = false
                                        //                                            if (ContactWatchService.getInstance() != null) {
//                                                Log.e("TAG", " starting again ==================");
//                                                ContactWatchService.getInstance().startContactObserver();
//                                            }
                                    }, 5000)
                                }
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            AppGlobals.deletedByMe = false
                            //                                if (ContactWatchService.getInstance() != null) {
//                                    Log.e("TAG", " starting again ==================");
//                                    ContactWatchService.getInstance().startContactObserver();
//                                }
                        }, 5000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleEmails(context: Context, id: String) {
        val contactsData: Contacts =
            AppDatabase.getDatabase(context).ContactDataDao().getContactInfo(id)
        Log.e("TAG", " main data " + (contactsData == null).toString())
        if (contactsData != null) {
            val cr = context.contentResolver
            val cur1 = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null
            )
            contactsData.personal_email = ""
            contactsData.work_email = ""
            contactsData.other_email = ""
            contactsData.custom_email = ""
            AppDatabase.getDatabase(
                context
            ).ContactDataDao().update(contactsData)

            while (cur1!!.moveToNext()) {
                //to get the contact names
                val emailType =
                    cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                Log.e("Name :", "type $emailType")
                //                            if (id.equals(ShowContact.this.id)) {
                val email =
                    cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                Log.e("Email", email!!)
                if (emailType != null && emailType == "1" && email != null) {
                    contactsData.personal_email = email
                } else if (emailType != null && emailType == "1" && email == null) {
                    contactsData.personal_email = ""
                }
                if (emailType != null && emailType == "2" && email != null) {
                    contactsData.work_email = email
                } else if (emailType != null && emailType == "2" && email == null) {
                    contactsData.work_email = ""
                }
                if (emailType != null && emailType == "3" && email != null) {
                    contactsData.other_email = email
                } else if (emailType != null && emailType == "3" && email == null) {
                    contactsData.other_email = ""
                }
                if (emailType == null && email != null) {
                    contactsData.custom_email = email
                } else if (emailType == null && email == null) {
                    contactsData.custom_email = ""
                }
                //                            }
            }
            AppDatabase.getDatabase(
                context
            ).ContactDataDao().update(contactsData)
            cur1.close()

            lastUpdated = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                AppGlobals.deletedByMe = false
            }, 5000)
        }
    }

    private fun processDelete(context: Context) {
        object : Thread() {
            override fun run() {
                super.run()
                val cr = context.contentResolver
                val cur = cr.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                var dataHashMap: HashMap<String?, String?>
                Log.e("TAG", "reading started for " + cur!!.count)
                if (cur.count > 0) {
                    while (cur.moveToNext()) {
                        dataHashMap = HashMap()
                        val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        val rawId =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                        dataHashMap["id"] = id
                        dataHashMap["raw_id"] = rawId
                        val name =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                .toInt() > 0
                        ) {
                            dataHashMap["name"] = name
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
                                    dataHashMap["custom"] = phone
                                }
                                if (type != null && type == "1" && phone != null) {
                                    dataHashMap["home_phone"] = phone
                                }
                                if (type != null && type == "2" && phone != null) {
                                    dataHashMap["mobile"] = phone
                                }
                                if (type != null && type == "7" && phone != null) {
                                    dataHashMap["other"] = phone
                                }
                                if (type != null && type == "3" && phone != null) {
                                    dataHashMap["work"] = phone
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
                            arrayList!!.add(dataHashMap)
                        }
                    }
                    cur.close()
                    processData(context)
                }
            }
        }.start()
    }

    private fun processData(context: Context) {
        val data = HashMap<String, HashMap<String, String>>()
        object : Thread() {
            override fun run() {
                super.run()
                val cr = context.contentResolver
                val cur: Cursor? = cr.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                if (cur!!.getCount() > 0) {
                    prefManager!!.count = cur.getCount()
                    while (cur.moveToNext()) {
                        val id: String =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        val cur1: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        //                if (this.id.equals(id)) {
                        while (cur1!!.moveToNext()) {
                            var emailType: String?=""
                            //to get the contact names
                            if(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)!=null)
                                emailType = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
//                            Log.e("Name :", "type $emailType")

                            //                            if (id.equals(ShowContact.this.id)) {
                            val email: String =
                                cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                            Log.e("Email", email)
                            if (emailType != null && emailType == "1" && email != null) {
                                if (data.containsKey(id)) {
                                    val innerMap: HashMap<String, String> =
                                        data[id] as HashMap<String, String>
                                    innerMap["home_email"] = email;
                                } else {
                                    val hashMap = HashMap<String, String>()
                                    hashMap["home_email"] = email
                                    data[id] = hashMap;
                                }
                            }
                            if (emailType != null && emailType == "2" && email != null) {
                                if (data.containsKey(id)) {
                                    val innerMap: HashMap<String, String> =
                                        data[id] as HashMap<String, String>
                                    innerMap["home_email"] = email;
                                } else {
                                    val hashMap = HashMap<String, String>()
                                    hashMap["work_email"] = email
                                    data[id] = hashMap;
                                }
                            }
                            if (emailType != null && emailType == "3" && email != null) {
                                if (data.containsKey(id)) {
                                    val innerMap: HashMap<String, String> =
                                        data[id] as HashMap<String, String>
                                    innerMap["home_email"] = email;
                                } else {
                                    val hashMap = HashMap<String, String>()
                                    hashMap["other_email"] = email
                                    data[id] = hashMap;
                                }
                            }
                            if (emailType == null && email != null) {
                                if (data.containsKey(id)) {
                                    val innerMap: HashMap<String, String> =
                                        data[id] as HashMap<String, String>
                                    innerMap["home_email"] = email;
                                } else {
                                    val hashMap = HashMap<String, String>()
                                    hashMap["custom_email"] = email
                                    data[id] = hashMap;
                                }
                            }
                            //                            }
                        }
                        cur1.close()
                    }
                    saveInDB(data)
                } else {
                    saveInDB(data)
                }
            }
        }.start()
    }

    private fun saveInDB(hashMap: HashMap<String, HashMap<String, String>>) {
        Log.e("Name :", "TOTAL " + arrayList!!.size)
        for (i in arrayList!!.indices) {
            val data = arrayList!![i]
            val mainData = Contacts()
            mainData.rawId = data["raw_id"].toString()
            mainData.id = data["id"].toString().toInt()
            mainData.name = data["name"].toString()
            if (data.containsKey("home_phone")) {
                mainData.home_no = data["home_phone"].toString()
            } else {
                mainData.home_no = ""
            }
            if (data.containsKey("mobile")) {
                mainData.mobile_no = data["mobile"].toString()
            } else {
                mainData.mobile_no = ""
            }
            if (data.containsKey("custom")) {
                mainData.custom_no = data["custom"].toString()
            } else {
                mainData.custom_no = ""
            }
            if (data.containsKey("other")) {
                mainData.other_no = data["other"].toString()
            } else {
                mainData.other_no = ""
            }
            if (data.containsKey("work")) {
                mainData.work_no = data["work"].toString()
            } else {
                mainData.work_no = ""
            }
            if (hashMap.containsKey(mainData.id.toString())) {
                val mailMap = hashMap[mainData.id.toString()]!!
                if (mailMap.containsKey("home_email")) {
                    mainData.personal_email = mailMap["home_email"].toString()
                } else {
                    mainData.personal_email = ""
                }
                if (mailMap.containsKey("custom_email")) {
                    mainData.custom_email = mailMap["custom_email"].toString()
                } else {
                    mainData.custom_email = ""
                }
                if (mailMap.containsKey("work_email")) {
                    mainData.work_email = mailMap["work_email"].toString()
                } else {
                    mainData.work_email = ""
                }
                if (mailMap.containsKey("other_email")) {
                    mainData.other_email = mailMap["other_email"].toString()
                } else {
                    mainData.other_email = ""
                }
            }
            AppDatabase.getDatabase(context).ContactDataDao().insert(mainData)
        }
    }
}