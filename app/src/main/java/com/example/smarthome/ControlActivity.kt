package com.example.smarthome

import android.Manifest
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smarthome.databinding.ControlLayoutBinding
import java.io.IOException
import java.lang.Exception
import java.util.*

class ControlActivity: AppCompatActivity(){
private var _binding: ControlLayoutBinding? = null
    private val binding get() = _binding!!
    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding= ControlLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        m_address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString()

        ConnectToDevice(this).execute()

        binding.controlLedOn.setOnClickListener { sendCommand("a") }
        binding.controlLedOff.setOnClickListener { sendCommand("b") }
        binding.controlLedDisconnect.setOnClickListener { disconnect()}
    }

    private fun sendCommand(input: String){
        if (m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch (e:  IOException){
                e.printStackTrace()
            }

        }
    }

    private fun disconnect(){
        if (m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
        finish()
    }
    private class ConnectToDevice(c:Context): AsyncTask<Void,Void,String>(){
        private var connectSuccess: Boolean = true
        private val context : Context
        init {
            this.context = c
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...","please wait")
        }

        override fun doInBackground(vararg p0: Void?): String {

            try {
                if (m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)

                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                        m_bluetoothSocket!!.connect()
                    }

                }
            }catch (e:Exception){
                connectSuccess = false
                e.printStackTrace()
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}