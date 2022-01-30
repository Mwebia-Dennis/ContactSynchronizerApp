package com.penguinstech.contactsync

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.penguinstech.contactsync.room.Contacts

class Adapter(val context: Context, private val contactsList: List<Contacts>) : RecyclerView.Adapter<Adapter.MyViewHolder>()  {


    val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(layoutInflater.inflate(R.layout.contact_card_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameTv.text = contactsList[position].name
        if(contactsList[position].mobile_no != "") {
            holder.phoneTv.text = contactsList[position].mobile_no
        }else{
            holder.phoneTv.text = contactsList[position].personal_email
        }
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv:TextView = itemView.findViewById<View>(R.id.name) as TextView
        val phoneTv:TextView = itemView.findViewById<View>(R.id.mobile_no) as TextView
    }
}