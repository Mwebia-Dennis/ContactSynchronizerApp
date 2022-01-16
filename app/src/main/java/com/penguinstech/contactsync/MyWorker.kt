package com.penguinstech.contactsync

import android.content.Context
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(private val context: Context, workerParams: WorkerParameters) : Worker(
        context, workerParams
) {
    private var contObserver: ContObserver? = null
    override fun doWork(): Result {
//        android.os.Debug.waitForDebugger()
        Log.e("TAG", "Do work called")
        WorkerHelper.scheduleWork(context, 60)
        try {
            if (contObserver != null) {
                context.contentResolver.unregisterContentObserver(contObserver!!)
            }
            //Registering contact observer
            contObserver = ContObserver(
                    Handler(context.mainLooper), context
            )
            context.contentResolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI,
                    true, contObserver!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Result.success()
    }
}