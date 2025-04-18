/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2025, pat733
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.esllet.launcher.vm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.esllet.launcher.utils.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class WifiVm private constructor() : ViewModel() {

    private val TAG = WifiVm::class.java.simpleName

    data class WifiStatus(val interfaceOn: Boolean, val ssid: String, val bssid: String, val networkType: String, val ip: String, val rssi: Int)

    companion object {
        private var instance: WifiVm? = null

        fun init(context: Context): WifiVm {
            return instance ?: synchronized(this) {
                instance ?: WifiVm().also {
                    instance = it
                    it.wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    it.registerNetworkCallback(context)
                }
            }
        }

        fun destroy(context: Context) {
            instance?.unregisterNetworkCallback(context)
            instance = null
        }

        fun getInstance() : WifiVm? {
            return instance
        }
    }

    private val STATUS_INTERFACE_OFF =  WifiStatus(false, "NULL", "NULL", "NULL", "NULL", 0)
    private val STATUS_DISCONNECTED =  WifiStatus(true, "NULL", "NULL", "NULL", "NULL", 0)
    private var wifiManager: WifiManager? = null

    val status = MutableStateFlow(STATUS_INTERFACE_OFF)

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN
                    )
                    val interfaceOff = wifiState == WifiManager.WIFI_STATE_DISABLED
                    LogUtil.d(TAG, "onReceive() interfaceOff = $interfaceOff")
                    if (interfaceOff) {
                        status.update { STATUS_INTERFACE_OFF }
                    }
                }
            }
        }
    }

    private val networkCallback = object : NetworkCallback() {
        override fun onCapabilitiesChanged(network : Network, networkCapabilities: NetworkCapabilities) {
            if (networkCapabilities.transportInfo == null) {
                wifiManager?.let {
                    if (it.connectionInfo != null) {
                        updateStatus(it.connectionInfo)
                    }
                }
            } else {
                val wifiInfo = networkCapabilities.transportInfo as WifiInfo
                updateStatus(wifiInfo)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            if (wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED)
                updateStatus(wifiManager!!.connectionInfo)
        }
    }

    private fun updateStatus(wifiInfo: WifiInfo) {
        LogUtil.d(TAG, "updateStatus() $wifiInfo")

        if (wifiManager != null && !wifiManager!!.isWifiEnabled) {
            status.update { STATUS_INTERFACE_OFF }
            return
        }

        if (wifiInfo.frequency == -1 || wifiInfo.ssid == null || wifiInfo.bssid == null) {
            status.update { STATUS_DISCONNECTED }
            return
        }

        val ipAddress = wifiInfo.ipAddress
        val ipString = String.format(Locale.getDefault(), "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff)

        val frequency = wifiInfo.frequency
        val networkType = if (frequency >= 4900 && frequency <= 5900)  "5GHz" else "2.4GHz"

        status.update { WifiStatus(
            true,
            wifiInfo.ssid.replace("\"", ""),
            wifiInfo.bssid,
            networkType,
            ipString,
            wifiInfo.rssi) }
    }

    fun registerNetworkCallback(context: Context) {
        wifiManager?.let {
            if (it.connectionInfo != null) {
                updateStatus(it.connectionInfo)
            }
        }

        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connMgr.registerNetworkCallback(request, networkCallback)

        val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiReceiver, filter)
    }

    fun unregisterNetworkCallback(context: Context) {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.unregisterNetworkCallback(networkCallback)
        context.unregisterReceiver(wifiReceiver)
    }
}