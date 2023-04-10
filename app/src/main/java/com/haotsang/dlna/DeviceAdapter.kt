package com.haotsang.dlna

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cybergarage.upnp.Device

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.Holder?>() {

    private var mDeviceList: List<Device> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<Device>) {
        mDeviceList = list
        notifyDataSetChanged()
    }

    fun getDevice(index: Int): Device {
        return mDeviceList[index]
    }

    fun getDeviceList(): List<Device> {
        return mDeviceList
    }

    var selection = 0
        @SuppressLint("NotifyDataSetChanged")
        set(choice) {
            field = choice
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val device: Device? = mDeviceList.getOrNull(position)
        holder.deviceName.text = device?.friendlyName

        holder.itemView.setBackgroundColor(if (selection == holder.adapterPosition) Color.BLUE else Color.TRANSPARENT)
    }

    override fun getItemCount(): Int = mDeviceList.size

    inner class Holder(contentView: View) : RecyclerView.ViewHolder(
        contentView) {
        var deviceName: TextView = contentView.findViewById(android.R.id.text1)
    }
}