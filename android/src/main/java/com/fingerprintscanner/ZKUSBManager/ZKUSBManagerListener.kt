package com.fingerprintscanner.ZKUSBManager

import android.hardware.usb.UsbDevice

interface ZKUSBManagerListener {
    //0 means success
    //-1 means device no found
    //-2 means device no permission
    fun onCheckPermission(result: Int)
    fun onUSBArrived(device: UsbDevice?)
    fun onUSBRemoved(device: UsbDevice?)
}
