package com.penguinstech.contactsync.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "contactsdata", indices = [Index(value = ["id"], unique = true)])
class Contacts : Serializable {
    @PrimaryKey
    var id = 0
    @ColumnInfo(name = "raw_id") var rawId: String? = null
    @ColumnInfo(name = "latomId") var latomId: String? = null
    @ColumnInfo(name = "name") var name: String? = ""
    @ColumnInfo(name = "lookupKey") var lookupKey: String? = null
    @ColumnInfo(name = "label") var label: String? = null
    @ColumnInfo(name = "image_uri") var imageUri: String? = null
    @ColumnInfo(name = "mobile_no") var mobile_no: String? = ""
    @ColumnInfo(name = "home_no") var home_no: String? = ""
    @ColumnInfo(name = "work_no") var work_no: String? = ""
    @ColumnInfo(name = "other_no") var other_no: String? = ""
    @ColumnInfo(name = "custom_no") var custom_no: String? = ""
    @ColumnInfo(name = "personal_email") var personal_email: String? = ""
    @ColumnInfo(name = "work_email") var work_email: String? = ""
    @ColumnInfo(name = "other_email") var other_email: String? = ""
    @ColumnInfo(name = "custom_email") var custom_email: String? = ""
    @ColumnInfo(name = "favorite") var favorite = false
    @ColumnInfo(name = "cont_add1") var cont_add1: String? = ""
    @ColumnInfo(name = "cont_add2") var cont_add2: String? = ""
    @ColumnInfo(name = "cont_add3") var cont_add3: String? = ""
    @ColumnInfo(name = "cont_city") var cont_city: String? = ""
    @ColumnInfo(name = "cont_state") var cont_state: String? = ""
    @ColumnInfo(name = "cont_country") var cont_country: String? = ""
    @ColumnInfo(name = "cont_pobox") var cont_pobox: String? = ""
}


