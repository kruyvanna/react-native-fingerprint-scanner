package com.fingerprintscanner.ZKUSBManager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import java.util.Random

/**
 * usb permission and hotplug
 */
class ZKUSBManager(context: Context, listener: ZKUSBManagerListener) {
    //usb's vendor id for zkteco
    private var vid = 0x1b55

    //usb's product id
    private var pid = 0

    //application context
    private var mContext: Context? = null
    private var ACTION_USB_PERMISSION: String = ""
    private var mbRegisterFilter = false
    private var zknirusbManagerListener: ZKUSBManagerListener? = null

//    class UsbMgrReceiver: BroadcastReceiver() {
//      override fun onReceive(context: Context, intent: Intent) {
//        val action = intent.action
//        if (ACTION_USB_PERMISSION == action) {
//          val device =
//            intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
//          if (device!!.vendorId == vid && device.productId == pid) {
//            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//              zknirusbManagerListener!!.onCheckPermission(0)
//            } else {
//              zknirusbManagerListener!!.onCheckPermission(-2)
//            }
//          }
//        } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
//          val device =
//            intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
//          if (device!!.vendorId == vid && device.productId == pid) {
//            zknirusbManagerListener!!.onUSBArrived(device)
//          }
//        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
//          val device =
//            intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
//          if (device!!.vendorId == vid && device.productId == pid) {
//            zknirusbManagerListener!!.onUSBRemoved(device)
//          }
//        }
//      }
//    }

    val usbMgrReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                val device =
                    intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device!!.vendorId == vid && device.productId == pid) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        zknirusbManagerListener!!.onCheckPermission(0)
                    } else {
                        zknirusbManagerListener!!.onCheckPermission(-2)
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                val device =
                    intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device!!.vendorId == vid && device.productId == pid) {
                    zknirusbManagerListener!!.onUSBArrived(device)
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device =
                    intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                if (device!!.vendorId == vid && device.productId == pid) {
                    zknirusbManagerListener!!.onUSBRemoved(device)
                }
            }
        }
    }

    private fun isNullOrEmpty(target: String?): Boolean {
        return if (null == target || "" == target || target.isEmpty()) {
            true
        } else false
    }

    private fun createRandomString(source: String, length: Int): String {
        if (this.isNullOrEmpty(source)) {
            return ""
        }
        val result = StringBuffer()
        val random = Random()
        for (index in 0 until length) {
            result.append(source[random.nextInt(source.length)])
        }
        return result.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registerUSBPermissionReceiver(): Boolean {
        if (null == mContext || mbRegisterFilter) {
            return false
        }
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        mContext!!.registerReceiver(usbMgrReceiver, filter, Context.RECEIVER_EXPORTED)
        mbRegisterFilter = true
        return true
    }

    fun unRegisterUSBPermissionReceiver() {
        if (null == mContext || !mbRegisterFilter) {
            return
        }
        mContext!!.unregisterReceiver(usbMgrReceiver)
        mbRegisterFilter = false
    }

    //End USB Permission
    /////////////////////////////////////////////
    init {
        if (null == context || null == listener) {
            throw NullPointerException("context or listener is null")
        }
        zknirusbManagerListener = listener
        ACTION_USB_PERMISSION = createRandomString(SOURCE_STRING, DEFAULT_LENGTH)
        mContext = context
    }

    //0 means success
    //-1 means device no found
    //-2 means device no permission
    @SuppressLint("MutableImplicitPendingIntent")
    fun initUSBPermission(vid: Int, pid: Int) {
        val usbManager = mContext!!.getSystemService(Context.USB_SERVICE) as UsbManager
        var usbDevice: UsbDevice? = null
        for (device in usbManager.deviceList.values) {
            val device_vid = device.vendorId
            val device_pid = device.productId
            if (device_vid == vid && device_pid == pid) {
                usbDevice = device
                break
            }
        }
        if (null == usbDevice) {
            zknirusbManagerListener!!.onCheckPermission(-1)
            return
        }
        this.vid = vid
        this.pid = pid
        if (!usbManager.hasPermission(usbDevice)) {
            val intent = Intent(ACTION_USB_PERMISSION)
            val pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
              PendingIntent.FLAG_MUTABLE)
            usbManager.requestPermission(usbDevice, pendingIntent)
        } else {
            zknirusbManagerListener!!.onCheckPermission(0)
        }
    }

    companion object {
        /////////////////////////////////////////////
        //for usb permission
        private const val SOURCE_STRING =
            "0123456789-_abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ"
        private const val DEFAULT_LENGTH = 16
    }
}
