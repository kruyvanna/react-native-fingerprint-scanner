package com.fingerprintscanner.util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

/**
 * @author Magic
 * @version 创建时间：2020/05/22 上午 9:44
 */
object PermissionUtils {
    fun checkPermissions(activity: Activity?, permissions: Array<String>): ArrayList<String> {
        if (activity == null) {
            throw NullPointerException("activity can't be null")
        }
        val deniedPermissions = ArrayList<String>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return deniedPermissions
        }
        for (permission in permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions
    }

    fun requestPermission(activity: Activity, permissions: Array<String>, requestCode: Int) {
        if (activity == null) {
            throw NullPointerException("activity can't be null")
        }
        ActivityCompat.requestPermissions(activity, permissions!!, requestCode)
    }
}
