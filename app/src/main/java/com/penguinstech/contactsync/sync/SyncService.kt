package com.penguinstech.contactsync.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncService: Service() {

    private val syncScheduler = SyncScheduler()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        syncScheduler.setScheduler(this)
        return START_STICKY
    }

}