package com.penguinstech.contactsync.sync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.penguinstech.contactsync.R
import com.penguinstech.contactsync.room.Friend

class FriendsAdapter(val context: Context, private val friendList: List<Friend>) : RecyclerView.Adapter<FriendsAdapter.MyViewHolder>()  {


    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(layoutInflater.inflate(R.layout.contact_card_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameTv.text = friendList[position].name
        holder.phoneTv.text = friendList[position].mobile_no
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView = itemView.findViewById<View>(R.id.name) as TextView
        val phoneTv: TextView = itemView.findViewById<View>(R.id.mobile_no) as TextView
    }
}