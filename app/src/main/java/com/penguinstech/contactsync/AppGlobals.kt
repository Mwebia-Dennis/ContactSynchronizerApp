package com.penguinstech.contactsync

import android.annotation.SuppressLint
import android.content.Context

class AppGlobals {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
        const val ChannelId = "contact_watch"

        @JvmField
        var deletedByMe = false
    }
}