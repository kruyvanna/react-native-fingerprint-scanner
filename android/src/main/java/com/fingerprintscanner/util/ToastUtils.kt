package com.fingerprintscanner.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {
    fun show(context: Context?, content: String?) {
        val toast = Toast.makeText(context, content, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}
