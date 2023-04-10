package com.haotsang.dlna

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setOnItemClickListener(onItemClickListener: ((holder: RecyclerView.ViewHolder, position: Int) -> Unit)?) {
    val mAttachListener: RecyclerView.OnChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            if (onItemClickListener != null) {
                view.setOnClickListener {
                    val holder = getChildViewHolder(it)
                    onItemClickListener.invoke(holder, holder.adapterPosition)
                }
            }
        }
        override fun onChildViewDetachedFromWindow(view: View) {}
    }

    addOnChildAttachStateChangeListener(mAttachListener)
//    removeOnChildAttachStateChangeListener(mAttachListener)
}
