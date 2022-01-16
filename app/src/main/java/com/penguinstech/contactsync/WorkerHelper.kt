package com.penguinstech.contactsync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkerHelper {

    private val WORK_TAG: String = "send_report_task"

    @JvmStatic
    fun scheduleWork(context: Context?, seconds: Int) {
        val diff: Long = TimeUnit.SECONDS.toMillis(seconds.toLong())
        val mWorkManager: WorkManager = WorkManager.getInstance((context)!!)

        //Contraints being set to the worker
        val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

        //Setting up the One Time Work Request
        val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(diff, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()

        //Enqueuing the worker
        mWorkManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.KEEP, mRequest)
        Log.e("TAG", "setting up timer")
    }
}