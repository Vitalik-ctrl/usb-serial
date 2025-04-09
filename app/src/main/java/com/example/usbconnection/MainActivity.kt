package com.example.usbconnection

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var usbManager: UsbManager
    private var serialPort: UsbSerialPort? = null
    private var usbSerialDriver: UsbSerialDriver? = null

    private lateinit var sendButton: Button
    private lateinit var inputEditText: EditText
    private lateinit var labelTextView: TextView

    companion object {
        const val ACTION_USB_PERMISSION = "com.example.usbconnection.USB_PERMISSION"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    labelTextView.append("Permission granted for device: ${device?.deviceName}\n")
                    usbSerialDriver?.let { setupSerialConnection(it) }
                        ?: labelTextView.append("Driver not found after permission granted\n")
                } else {
                    labelTextView.append("Permission denied for device\n")
                }
            }
        }
    }

    private val usbAttachReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let {
                    labelTextView.append("USB device attached: ${it.deviceName}\n")
                    handleUsbDevice(it)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MutableImplicitPendingIntent")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        sendButton = findViewById(R.id.sendButton)
        inputEditText = findViewById(R.id.inputEditText)
        labelTextView = findViewById(R.id.labelTextView)

        registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION), RECEIVER_NOT_EXPORTED)
        registerReceiver(usbAttachReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED), RECEIVER_NOT_EXPORTED)

        scanForUsbDevices()

        sendButton.setOnClickListener {
            val textToSend = inputEditText.text.toString()
            writeData(textToSend)
        }
    }

    private fun scanForUsbDevices() {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (drivers.isEmpty()) {
            labelTextView.append("No USB serial device found\n")
            return
        }

        drivers.forEach { driver ->
            val device = driver.device
            labelTextView.append("Found USB device: ${device.deviceName} (Vendor: ${device.vendorId}, Product: ${device.productId})\n")
            handleUsbDevice(device)
        }
    }

    private fun handleUsbDevice(device: UsbDevice) {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val matchingDriver = drivers.find { it.device.deviceId == device.deviceId }

        if (matchingDriver != null) {
            usbSerialDriver = matchingDriver
            if (!usbManager.hasPermission(device)) {
                val permissionIntent = PendingIntent.getBroadcast(
                    this, 0,
                    Intent(ACTION_USB_PERMISSION).apply { setPackage(packageName) },
                    PendingIntent.FLAG_MUTABLE
                )
                usbManager.requestPermission(device, permissionIntent)
                labelTextView.append("Requesting permission for ${device.deviceName}\n")
            } else {
                setupSerialConnection(matchingDriver)
            }
        } else {
            labelTextView.append("No matching serial driver found for ${device.deviceName}\n")
        }
    }

    private fun setupSerialConnection(driver: UsbSerialDriver) {
        if (driver.ports.isEmpty()) {
            labelTextView.append("No ports available on device\n")
            return
        }

        try {
            val connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                labelTextView.append("Cannot open device\n")
                return
            }

            serialPort = driver.ports[0]
            serialPort?.open(connection)
            serialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            labelTextView.append("Serial port opened on ${driver.device.deviceName}\n")
            startReading()
        } catch (e: Exception) {
            e.printStackTrace()
            labelTextView.append("Error: ${e.message}\n")
        }
    }

    private fun writeData(data: String) {
        if (serialPort == null) {
            labelTextView.append("Serial port not opened\n")
            return
        }

        try {
            val bytes = data.toByteArray()
            serialPort?.write(bytes, 1000)
            labelTextView.append("Sent: $data\n")
        } catch (e: IOException) {
            e.printStackTrace()
            labelTextView.append("Write error: ${e.message}\n")
        }
    }

    private fun startReading() {
        Thread {
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val len = serialPort?.read(buffer, 1000) ?: break
                    if (len > 0) {
                        val received = String(buffer, 0, len)
                        runOnUiThread {
                            labelTextView.append("Received: $received\n")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        labelTextView.append("Read error: ${e.message}\n")
                    }
                    break
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        unregisterReceiver(usbAttachReceiver)
        try {
            serialPort?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
