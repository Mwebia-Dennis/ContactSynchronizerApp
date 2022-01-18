package com.penguinstech.contactsync

class User( ) {
    var userId:String?= null
    var userName:String?= null
    var phoneNo:String?= null
    constructor(userId:String, userName:String, phoneNo:String) : this() {
        this.userId = userId
        this.userName = userName
        this.phoneNo = phoneNo
    }
}