package com.example.brockapp.interfaces

import com.example.brockapp.R

import android.widget.Toast
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.view.LayoutInflater

class ShowCustomToastImpl: ShowCustomToast {
    override fun showBasicToast(message: String, context: Context) {
        // Previously I create an empty Toast, where I will put the layout and the text
        val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val parent: ViewGroup? = null
        val myBasicToast = inflater.inflate(R.layout.toast_basic, parent)
        myBasicToast.findViewById<TextView>(R.id.text_view_toast_basic).setText(message)

        // Finally I set the View with mine Toast View
        toast.view = myBasicToast
        toast.show()
    }

    override fun showWarningToast(message: String, context: Context) {
        val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val parent: ViewGroup? = null
        val myWarningToast = inflater.inflate(R.layout.toast_warning, parent)
        myWarningToast.findViewById<TextView>(R.id.text_view_toast_warning).setText(message)

        toast.view = myWarningToast
        toast.show()
    }
}