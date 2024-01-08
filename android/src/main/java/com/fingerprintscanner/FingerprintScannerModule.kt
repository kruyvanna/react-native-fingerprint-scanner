package com.fingerprintscanner

import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.fingerprintscanner.ZKUSBManager.ZKUSBManager
import com.fingerprintscanner.ZKUSBManager.ZKUSBManagerListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.zkteco.android.biometric.FingerprintExceptionListener
import com.zkteco.android.biometric.core.device.ParameterHelper
import com.zkteco.android.biometric.core.device.TransportType
import com.zkteco.android.biometric.core.utils.LogHelper
import com.zkteco.android.biometric.core.utils.ToolUtils
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintCaptureListener
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintSensor
import com.zkteco.android.biometric.module.fingerprintreader.FingprintFactory
import com.zkteco.android.biometric.module.fingerprintreader.exception.FingerprintException
import java.io.ByteArrayOutputStream

@ReactModule(name = FingerprintScannerModule.NAME)
class FingerprintScannerModule(reactContext: ReactApplicationContext) :
  NativeFingerprintScannerSpec(reactContext) {

  private val REQUEST_PERMISSION_CODE = 9
  private var zkusbManager: ZKUSBManager? = null
  private var fingerprintSensor: FingerprintSensor? = null
  private val usb_vid = ZKTECO_VID
  private var usb_pid = 0
  private val deviceIndex = 0
  private var isReseted = false

  @RequiresApi(Build.VERSION_CODES.O)
  override fun setup() {
    zkusbManager = ZKUSBManager(this.reactApplicationContext, zkusbManagerListener)
    zkusbManager?.registerUSBPermissionReceiver()
  }

  override fun connectDevice() {
    enumSensor()
    tryGetUSBPermission()
  }

  override fun disconnectDevice() {
    closeDevice()
  }

  override fun getName(): String {
    return NAME
  }

  fun onDeviceConnected() {
    // send event to js side
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onDeviceConnected", null)
  }

  fun onDeviceDisconnected() {
    // send event to js side
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onDeviceDisconnected", null)
  }

  fun onGotImage(result: String) {
    // send event to js side
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onGotImage", result)
  }

  fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
  }

  private val fingerprintCaptureListener: FingerprintCaptureListener =
    object : FingerprintCaptureListener {
      override fun captureOK(fpImage: ByteArray) {
        val bitmap = ToolUtils.renderCroppedGreyScaleBitmap(
          fpImage,
          fingerprintSensor!!.imageWidth,
          fingerprintSensor!!.imageHeight
        )
        val base64Image = bitmapToBase64(bitmap)
        onGotImage(base64Image)
      }

      override fun captureError(e: FingerprintException) {
        // nothing to do
        e.printStackTrace()
      }

      override fun extractOK(fpTemplate: ByteArray) {

      }

      override fun extractError(i: Int) {
        // nothing to do
        Log.i(TAG, "extractError $i")
      }
    }
    
  private val fingerprintExceptionListener = FingerprintExceptionListener {
    LogHelper.e("[] usb exception!!!")
    if (!isReseted) {
      try {
        fingerprintSensor!!.openAndReboot(deviceIndex)
      } catch (e: FingerprintException) {
        e.printStackTrace()
      }
      isReseted = true
    }
  }
  private val zkusbManagerListener: ZKUSBManagerListener = object : ZKUSBManagerListener {
    override fun onCheckPermission(result: Int) {
      afterGetUsbPermission()
    }

    override fun onUSBArrived(device: UsbDevice?) {
      tryGetUSBPermission()
    }

    override fun onUSBRemoved(device: UsbDevice?) {
      closeDevice()
//      scanViewModel.setDeviceConnected(false)
    }

  }

  private fun createFingerprintSensor() {
    if (null != fingerprintSensor) {
      FingprintFactory.destroy(fingerprintSensor)
      fingerprintSensor = null
    }
    // Define output log level
    LogHelper.setLevel(Log.ERROR)
    LogHelper.setNDKLogLevel(Log.ASSERT)
    // Start fingerprint sensor
    val deviceParams: MutableMap<String, Any?> = HashMap<String, Any?>()
    //set vid
    deviceParams[ParameterHelper.PARAM_KEY_VID] = usb_vid
    //set pid
    deviceParams[ParameterHelper.PARAM_KEY_PID] = usb_pid
    fingerprintSensor = FingprintFactory.createFingerprintSensor(
      this.reactApplicationContext,
      TransportType.USB,
      deviceParams
    )
  }

  private fun enumSensor(): Boolean {
    val usbManager = this.reactApplicationContext.getSystemService(ComponentActivity.USB_SERVICE) as UsbManager
    for (device in usbManager.deviceList.values) {
      val device_vid = device.vendorId
      val device_pid = device.productId
      if (device_vid == ZKTECO_VID && (device_pid == LIVE20R_PID || device_pid == LIVE10R_PID)) {
        usb_pid = device_pid
        return true
      }
    }
    return false
  }

  private fun tryGetUSBPermission() {
    zkusbManager?.initUSBPermission(usb_vid, usb_pid)
  }

  private fun afterGetUsbPermission() {
    openDevice()
  }

  private fun openDevice() {
    createFingerprintSensor()
    isReseted = false
    try {
      //fingerprintSensor.setCaptureMode(1);
      fingerprintSensor?.open(deviceIndex)

      run {
        // device parameter
        LogHelper.d("sdk version" + fingerprintSensor!!.sdK_Version)
        LogHelper.d("firmware version" + fingerprintSensor!!.firmwareVersion)
        LogHelper.d("serial:" + fingerprintSensor!!.strSerialNumber)
        LogHelper.d("width=" + fingerprintSensor!!.imageWidth + ", height=" + fingerprintSensor!!.imageHeight)
//        LogHelper.setNDKLogLevel(Log.ERROR)
//        LogHelper.setLevel(Log.ERROR)
      }
      fingerprintSensor?.setFingerprintCaptureListener(
        deviceIndex,
        fingerprintCaptureListener
      )
      fingerprintSensor?.SetFingerprintExceptionListener(fingerprintExceptionListener)
      fingerprintSensor?.startCapture(deviceIndex)
//      scanViewModel.setDeviceConnected(true)
      onDeviceConnected()
    } catch (e: FingerprintException) {
      e.printStackTrace()
      // try to  reboot the sensor
      try {
        fingerprintSensor!!.openAndReboot(deviceIndex)
      } catch (ex: FingerprintException) {
        ex.printStackTrace()
      }
//      scanViewModel.setDeviceConnected(false)
      onDeviceDisconnected()
    }
  }

  private fun closeDevice() {
    try {
      fingerprintSensor?.stopCapture(deviceIndex)
      fingerprintSensor?.close(deviceIndex)
//      scanViewModel.setDeviceConnected(false)
      onDeviceDisconnected()

    } catch (e: FingerprintException) {
      e.printStackTrace()
    }
  }

  companion object {
    private const val ZKTECO_VID = 0x1b55
    private const val LIVE20R_PID = 0x0120
    private const val LIVE10R_PID = 0x0124
    const val NAME = "FingerprintScanner"
    private const val TAG = "Fingerprint"
  }
}
